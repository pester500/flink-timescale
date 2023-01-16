package com.flink.logs

import java.sql.Types.{INTEGER, TIMESTAMP, VARCHAR}
import java.util.Properties
import com.flink.config.{AppConfig, Constants}
import com.flink.kafka.KafkaClient
import com.flink.logs.dto.{KafkaLogMessage, LogEntry}
import com.flink.logs.schema.LogEntrySchema
import grizzled.slf4j.Logging
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.java.io.jdbc.JDBCSinkFunction
import org.apache.flink.connector.jdbc.JdbcRowOutputFormat
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.flink.types.Row
import org.flywaydb.core.Flyway

class LogsFlow extends Constants with LogsConstants with KafkaClient with Serializable with Logging {

  private lazy val config = new AppConfig

  def execute(): Unit = {
    createTopic()

    logger.info("Starting the flyway migration")
    lazy val flyway = Flyway.configure.dataSource(config.pgUrl, config.pgUser, config.pgPass).load()
    flyway.migrate()
    logger.info("Finished the flyway migration")

    lazy val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setMaxParallelism(config.maxParallelism)

    lazy val properties = new Properties()
    properties.setProperty(BOOTSTRAP_SERVERS, config.bootstrapServer)
    properties.setProperty(GROUP_ID, config.groupId)
    properties.setProperty(AUTO_OFFSET_RESET, EARLIEST)

    lazy val kafkaConsumer = KafkaSource.builder[KafkaLogMessage]
      .setBootstrapServers(config.bootstrapServer)
      .setGroupId(config.groupId)
      .setTopics(config.topicName)
      .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(LogEntrySchema))
      .setStartingOffsets(OffsetsInitializer.earliest)
      .build


    lazy val successJdbcOutput = JdbcRowOutputFormat
      .buildJdbcOutputFormat
      .setDrivername(POSTGRES_DRIVER)
      .setDBUrl(config.pgUrl)
      .setUsername(config.pgUser)
      .setPassword(config.pgPass)
      .setQuery(successQuery)
      .setSqlTypes(Array[Int](VARCHAR, TIMESTAMP, TIMESTAMP, INTEGER, VARCHAR, VARCHAR))
      .finish()

    env.fromSource(kafkaConsumer, WatermarkStrategy.noWatermarks[KafkaLogMessage], logTableName)
      .map[LogEntry](LogMapper).name("Parse Kafka CSV message")
      .map[Row](LogRowMapper)
      .addSink(new JDBCSinkFunction(successJdbcOutput))

    env.execute("flink-logs")
  }
}
