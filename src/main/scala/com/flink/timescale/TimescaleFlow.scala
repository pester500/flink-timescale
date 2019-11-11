package com.flink.timescale

import java.sql.Types._
import java.util.Properties

import com.flink.timescale.config.AppConfig
import com.flink.timescale.dto.CrimeMessage
import com.flink.timescale.operators.{WindowedDistrictCrimeAggregator, _}
import com.flink.timescale.schemas.KafkaStringSchema
import grizzled.slf4j.Logging
import org.apache.flink.api.common.serialization.SimpleStringEncoder
import org.apache.flink.api.java.io.jdbc.{JDBCOutputFormat, JDBCSinkFunction}
import org.apache.flink.core.fs.Path
import org.apache.flink.runtime.state.StateBackend
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.types.Row
import org.flywaydb.core.Flyway


class TimescaleFlow extends Constants with Serializable with Logging {

  private lazy val config = new AppConfig

  def execute(): Unit = {

    // Create schema in Timescale DB
    logger.info("Starting the flyway migration")
    lazy val flyway = Flyway.configure.dataSource(config.url, config.user, config.pass).load()
    flyway.migrate()
    logger.info("Finished the flyway migration")

    val fsBackend: StateBackend = new FsStateBackend("file:///tmp/flink/checkpoints", true)

    // Create local reference to Flink execution env and set state backend for stream job
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
    env.setStateBackend(fsBackend)
    env.enableCheckpointing(5000)
    env.setMaxParallelism(config.maxParallelism)


    /**
     * SET UP KAFKA SOURCE AND VARIOUS SINK OUTPUT FORMATS
     */

    // Create Kafka consumer with properties
    val properties = new Properties()
    properties.setProperty(BOOTSTRAP_SERVERS, config.bootstrapServer)
    properties.setProperty(GROUP_ID, config.groupId)
    properties.setProperty(AUTO_OFFSET_RESET, EARLIEST)
    lazy val kafkaConsumer = new FlinkKafkaConsumer[String](config.source, KafkaStringSchema, properties)

    // Create JDBC Output with a reference to success INSERT query
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

    // Create JDBC Output with reference to failure INSERT query
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

    lazy val processingSpeedOutput = JDBCOutputFormat
      .buildJDBCOutputFormat()
      .setDrivername(POSTGRES_DRIVER)
      .setDBUrl(config.url)
      .setUsername(config.user)
      .setPassword(config.pass)
      .setQuery(processingSpeedQuery)
      .setBatchInterval(10000)
      .setSqlTypes(Array[Int](VARCHAR, TIMESTAMP))
      .finish


    /**
     * SET UP FLINK SOURCE TO READ FROM KAFKA
     */

    // Read CSV lines from Kafka and return Either[String, CrimeMessage]
    val dataStream: SplitStream[Either[String, CrimeMessage]] = env.addSource(kafkaConsumer)
      .map[Either[String, CrimeMessage]](new CrimeMessageMapper).name("Parse Kafka CSV message")
      .split(new CrimesStreamSplitter)

    /**
     * DEFINE 2 SINKS (SUCCESS AND FAILURE) TO WRITE RECORDS TO TIMESCALE DB
     */

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


    /**
     * DEFINE FILE SINK
     */

    // Create File Sink for windowed stream
    lazy val fileSink =
      StreamingFileSink.forRowFormat(new Path("/tmp/write/"), new SimpleStringEncoder[CrimeMessage]("UTF-8"))
        .build()

    //Group stream elements by police district for a summation of all crimes in stream
    val crimesByDistrict =
          dataStream.select(PARSED)
          .map(_.right.get).name("Extract crime from Either[CrimeMessage, String]")
          .keyBy(_.district)
          .timeWindow(Time.seconds(1))
          .aggregate(WindowedDistrictCrimeAggregator).name("Count number of records processed per second")

//      .addSink(new JDBCSinkFunction(processingSpeedOutput))

    crimesByDistrict
//    crimesByDistrict

    env.execute("flink-timescale")
  }

}
