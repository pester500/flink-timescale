package com.flink.timescale

import java.sql.Types._
import java.util.Properties

import com.flink.timescale.config.AppConfig
import com.flink.timescale.dto.CrimeMessage
import com.flink.timescale.operators.{CrimeMessageMapper, CrimesStreamSplitter, FailureRowMapper, SuccessRowMapper}
import grizzled.slf4j.Logging
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.io.jdbc.JDBCOutputFormat
import org.apache.flink.api.java.io.jdbc.JDBCSinkFunction
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.aggregation.AggregationFunction.AggregationType
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.ContinuousProcessingTimeTrigger
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.types.Row
import org.flywaydb.core.Flyway

import scala.util.{Failure, Success, Try}

class TimescaleFlow extends Constants with Serializable with Logging {

  private lazy val config = new AppConfig

  def execute(): Unit = {

    // Create schema for JDBC output
    logger.info("Starting the flyway migration")
    lazy val flyway = Flyway.configure.dataSource(config.url, config.user, config.pass).load()
    flyway.migrate()
    logger.info("Finished the flyway migration")

    // Create local reference to Flink execution env and set state backend for stream re
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
    env.setStateBackend(new FsStateBackend("file:///tmp/flink/checkpoints"))
    env.enableCheckpointing(5000)
    env.setMaxParallelism(config.maxParallelism)

    // Create Kafka consumer with properties
    val properties = new Properties()
    properties.setProperty(BOOTSTRAP_SERVERS, config.bootstrapServer)
    properties.setProperty(GROUP_ID, config.groupId)
    properties.setProperty(AUTO_OFFSET_RESET, EARLIEST)
    lazy val kafkaConsumer = new FlinkKafkaConsumer[String](config.source, KafkaStringSchema, properties)

    // Create JDBC connection with reference to success INSERT query
    lazy val successJdbcOutput = JDBCOutputFormat
      .buildJDBCOutputFormat
      .setDrivername(POSTGRES_DRIVER)
      .setDBUrl(config.url)
      .setUsername(config.user)
      .setPassword(config.pass)
      .setQuery(successQuery)
      .setBatchInterval(10000)
      .setSqlTypes(Array[Int](
        INTEGER, VARCHAR, TIMESTAMP, VARCHAR, VARCHAR,
        VARCHAR, VARCHAR, VARCHAR, BOOLEAN, BOOLEAN,
        VARCHAR, VARCHAR, INTEGER, VARCHAR, VARCHAR,
        DOUBLE, DOUBLE, INTEGER, TIMESTAMP, DOUBLE,
        DOUBLE, VARCHAR, TIMESTAMP
      ))
      .finish

    // Create JDBC connection with reference to failure INSERT query
    lazy val failureJdbcOutput = JDBCOutputFormat
      .buildJDBCOutputFormat
      .setDrivername(POSTGRES_DRIVER)
      .setDBUrl(config.url)
      .setUsername(config.user)
      .setPassword(config.pass)
      .setQuery(failQuery)
      .setBatchInterval(10000)
      .setSqlTypes(Array[Int](VARCHAR, TIMESTAMP))
      .finish

    // Read CSV lines from Kafka and return Either[String, CrimeMessage]
    val dataStream: SplitStream[Either[String, CrimeMessage]] = env.addSource(kafkaConsumer)
      .map[Either[String, CrimeMessage]](new CrimeMessageMapper).name("Parse Kafka CSV message")
      .split(new CrimesStreamSplitter)

    // Select failure stream and write records to database for human inspection at later time
    dataStream
      .select(NOT_PARSED)
      .map[Row](new FailureRowMapper).name("Save failures for future inspection")
      .addSink(new JDBCSinkFunction(failureJdbcOutput)).name("Write failures to database")

    // Select success stream and write records to Timescale hypertable for quick querying
    dataStream
      .select(PARSED)
      .map[Row](new SuccessRowMapper).name("Convert to Row")
      .addSink(new JDBCSinkFunction(successJdbcOutput)).name("Insert into Timescale crimes table")

    //Group stream elements by police district for a summation of all crimes in stream
    val crimesByDistrict =
      dataStream.select(PARSED)
      .map(value => value.right.get).name("Extract crime from Either[CrimeMessage, String]")
      .keyBy(value => value.district)
      .window(GlobalWindows.create())
      .trigger(ContinuousProcessingTimeTrigger.of(Time.seconds(5)))
      .aggregate(AggregationType.SUM, 12)

    crimesByDistrict.writeAsCsv("/tmp/write/")


    env.execute("flink-timescale")
  }

  object KafkaStringSchema extends DeserializationSchema[String] {

    override def isEndOfStream(t: String): Boolean = false

    override def deserialize(bytes: Array[Byte]): String = {
      Try(new String(bytes, "UTF-8")) match {
        case Success(value) => value
        case Failure(e) =>
          logger.error(s"Error serializing crime details: $e")
          NULL_FROM_KAFKA
      }
    }

    override def getProducedType: TypeInformation[String] = TypeExtractor.getForClass(classOf[String])
  }

}
