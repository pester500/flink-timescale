package com.flink.timescale

import java.util.Properties

import com.flink.timescale.config.AppConfig
import com.flink.timescale.dto.WordMessage
import com.flink.timescale.operators.{RowMapper, WordMessageMapper}
import grizzled.slf4j.Logging
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.io.jdbc.JDBCOutputFormat
import org.apache.flink.api.java.io.jdbc.JDBCSinkFunction
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.types.Row
import org.flywaydb.core.Flyway

class TimescaleFlow extends Constants with Serializable with Logging {

  lazy val config = new AppConfig

  def execute(): Unit = {

    logger.info("Starting the flyway migration")
    lazy val flyway = Flyway.configure.dataSource(config.url, config.user, config.pass).load()
    flyway.migrate()
    logger.info("Finished the flyway migration")

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
    env.setMaxParallelism(config.maxParallelism)

    val properties = new Properties()
    properties.setProperty(BOOTSTRAP_SERVERS, config.bootstrapServer)
    properties.setProperty(GROUP_ID, config.groupId)
    properties.setProperty(AUTO_OFFSET_RESET, LATEST)

    lazy val kafkaConsumer = new FlinkKafkaConsumer[String](config.source, KafkaStringSchema, properties)

    implicit val kafkaStringTypeInformation: TypeInformation[String] = KafkaStringSchema.getProducedType
    implicit val wordMessageMapperTypeInformation: TypeInformation[WordMessage] = createTypeInformation[WordMessage]
    implicit val rowMapperTypeInformation: TypeInformation[Row] = createTypeInformation[Row]

    lazy val jdbcOutput = JDBCOutputFormat
      .buildJDBCOutputFormat
      .setDrivername("org.postgresql.Driver")
      .setDBUrl(config.url)
      .setUsername(config.user)
      .setPassword(config.pass)
      .setQuery(query)
      .setBatchInterval(10000)
      .finish

    env.addSource(kafkaConsumer)
      .map(new WordMessageMapper).name("Add timestamp")
      .filter(_ != null)
      .map(new RowMapper).name("Convert to Row")
      .addSink(new JDBCSinkFunction(jdbcOutput)).name("Insert into Timescale")


    env.execute("flink-timescale")
  }

  object KafkaStringSchema extends DeserializationSchema[String] {

    override def isEndOfStream(t: String): Boolean = false

    override def deserialize(bytes: Array[Byte]): String = new String(bytes, "UTF-8")

    override def getProducedType: TypeInformation[String] = TypeExtractor.getForClass(classOf[String])
  }

}
