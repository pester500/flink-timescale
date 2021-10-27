package com.flink

import java.sql.Types._
import com.flink.config.{AppConfig, Constants}
import com.flink.dto.CrimeMessage
import com.flink.operators.{CrimeMessageMapper, SuccessRowMapper}
import com.flink.schema.KafkaStringSchema
import grizzled.slf4j.Logging
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.java.io.jdbc.JDBCSinkFunction
import org.apache.flink.connector.jdbc.JdbcRowOutputFormat
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.types.Row
import org.flywaydb.core.Flyway


class TimescaleFlow extends Constants with CrimesConstants with Serializable with Logging {

  private lazy val config = new AppConfig

  def execute(): Unit = {

    // Create schema for JDBC output
    logger.info("Starting the flyway migration")
    lazy val flyway = Flyway.configure.dataSource(config.pgUrl, config.pgUser, config.pgPass).load()
    flyway.migrate()
    logger.info("Finished the flyway migration")

    // Create local reference to Flink execution env and set state backend for stream
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setMaxParallelism(config.maxParallelism)

    lazy val kafkaConsumer = KafkaSource.builder[String]
      .setBootstrapServers(config.bootstrapServer)
      .setGroupId(config.groupId)
      .setTopics(config.crimesSource)
      .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(KafkaStringSchema))
      .setStartingOffsets(OffsetsInitializer.earliest)
      .build

    // Create JDBC connection with reference to success INSERT query
    lazy val successJdbcOutput = JdbcRowOutputFormat
      .buildJdbcOutputFormat
      .setDrivername(POSTGRES_DRIVER)
      .setDBUrl(config.pgUrl)
      .setUsername(config.pgUser)
      .setPassword(config.pgPass)
      .setQuery(successQuery)
      .setSqlTypes(Array[Int](
        INTEGER, VARCHAR, TIMESTAMP, VARCHAR, VARCHAR,
        VARCHAR, VARCHAR, VARCHAR, BOOLEAN, BOOLEAN,
        VARCHAR, VARCHAR, INTEGER, VARCHAR, VARCHAR,
        DOUBLE, DOUBLE, INTEGER, TIMESTAMP, DOUBLE,
        DOUBLE, VARCHAR, TIMESTAMP
      ))
      .finish

    // Create JDBC connection with reference to failure INSERT query
    lazy val failureJdbcOutput = JdbcRowOutputFormat
      .buildJdbcOutputFormat
      .setDrivername(POSTGRES_DRIVER)
      .setDBUrl(config.pgUrl)
      .setUsername(config.pgUser)
      .setPassword(config.pgPass)
      .setQuery(failQuery)
      .setSqlTypes(Array[Int](VARCHAR, TIMESTAMP))
      .finish

    // Read CSV lines from Kafka and return Either[String, CrimeMessage]
    env.fromSource(kafkaConsumer, WatermarkStrategy.noWatermarks[String], crimesTable)
      .map[Either[String, CrimeMessage]](new CrimeMessageMapper).name("Parse Kafka CSV message")
      .map[Row](new SuccessRowMapper).name("Convert to Row")
      .addSink(new JDBCSinkFunction(successJdbcOutput)).name("Insert into Timescale crimes table")

    //Group stream elements by police district for a summation of all crimes in stream
//    val crimesByDistrict =
//      dataStream.select(PARSED)
//      .map(value => value.right.get).name("Extract crime from Either[CrimeMessage, String]")
//      .keyBy(value => value.district)
//      .window(GlobalWindows.create())
//      .trigger(ContinuousProcessingTimeTrigger.of(Time.seconds(5)))
//      .aggregate(AggregationType.SUM, 12)

//    crimesByDistrict.writeAsCsv("/tmp/write/")


    env.execute("flink-timescale")
  }

}
