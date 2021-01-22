package com.flink.logs.schema

import com.flink.config.Constants
import com.flink.logs.dto.KafkaLogMessage
import grizzled.slf4j.Logging
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization

import scala.util.{Failure, Success, Try}

object LogEntrySchema extends DeserializationSchema[KafkaLogMessage] with Logging with Constants with Serializable {

  private implicit lazy val formats: Formats = Serialization.formats(NoTypeHints)

  override def isEndOfStream(t: KafkaLogMessage): Boolean = false

  override def deserialize(bytes: Array[Byte]): KafkaLogMessage = {
    Try(parse(new String(bytes, "UTF-8")).extract[KafkaLogMessage]) match {
      case Success(value) => value
      case Failure(e) =>
        logger.error(s"Error serializing crime details: $e")
        throw new RuntimeException(s"wat: $e")
    }
  }

  override def getProducedType: TypeInformation[KafkaLogMessage] = TypeExtractor.getForClass(classOf[KafkaLogMessage])
}
