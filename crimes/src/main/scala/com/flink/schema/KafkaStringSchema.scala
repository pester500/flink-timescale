package com.flink.schema

import com.flink.config.Constants
import grizzled.slf4j.Logging
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor

import scala.util.{Failure, Success, Try}

object KafkaStringSchema extends DeserializationSchema[String] with Logging with Constants with Serializable {

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
