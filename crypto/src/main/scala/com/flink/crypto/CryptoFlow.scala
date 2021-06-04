package com.flink.crypto

import com.flink.config.{AppConfig, Constants}
import grizzled.slf4j.Logging
import org.apache.flink.api.java.io.jdbc.JDBCSinkFunction
import org.apache.flink.connector.jdbc.JdbcOutputFormat
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.types.Row
import org.flywaydb.core.Flyway

import java.sql.Types.{INTEGER, TIMESTAMP, VARCHAR}
import java.util.Properties

class CryptoFlow extends Constants with Logging {

  private lazy val config = new AppConfig

  def execute(): Unit = {
    logger.info("Starting the flyway migration")
    lazy val flyway = Flyway.configure.dataSource(config.pgUrl, config.pgUser, config.pgPass).load()
    flyway.migrate()
    logger.info("Finished the flyway migration")

    lazy val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStateBackend(new FsStateBackend("file:///tmp/flink/checkpoints"))
    env.enableCheckpointing(5000)
    env.setMaxParallelism(config.maxParallelism)

    lazy val properties = new Properties()
    properties.setProperty(BOOTSTRAP_SERVERS, config.bootstrapServer)
    properties.setProperty(GROUP_ID, config.groupId)
    properties.setProperty(AUTO_OFFSET_RESET, EARLIEST)
    lazy val kafkaConsumer = new FlinkKafkaConsumer[KafkaLogMessage](config.logsSource, LogEntrySchema, properties)

    lazy val successJdbcOutput = JdbcOutputFormat
      .buildJdbcOutputFormat
      .setDrivername(POSTGRES_DRIVER)
      .setDBUrl(config.pgUrl)
      .setUsername(config.pgUser)
      .setPassword(config.pgPass)
      .setQuery(successQuery)
      .setSqlTypes(Array[Int](VARCHAR, TIMESTAMP, TIMESTAMP, INTEGER, VARCHAR, VARCHAR))
      .finish

    env.addSource(kafkaConsumer)
      .map[LogEntry](LogMapper).name("Parse Kafka CSV message")
      .map[Row](LogRowMapper)
      .addSink(new JDBCSinkFunction(successJdbcOutput))

    env.execute("flink-logs")
  }
}
