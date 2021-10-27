package com.flink.logs

import com.flink.logs.dto.ZipUtils.unzipString

import java.sql.Timestamp
import java.time.Instant
import com.flink.logs.dto.{KafkaLogMessage, LogEntry}
import grizzled.slf4j.Logging
import org.apache.flink.api.common.functions.MapFunction

import scala.util.{Failure, Success, Try}

object LogMapper extends MapFunction[KafkaLogMessage, LogEntry] with Serializable with Logging {

  override def map(value: KafkaLogMessage): LogEntry = {
    val distinctCharacters = value.logEntry.toSet
    val charCount = distinctCharacters.map(char => (char.toString, value.logEntry.count(_ == char))).toMap
    val entry = Try(unzipString(value.logEntry)) match {
      case Success(v) => new String(v)
      case Failure(exception) =>
        logger.warn(s"error unzipping log: ${exception.toString}")
        ""
    }
    val ingested = value.timeIngested
    val processed = Timestamp.from(Instant.now())
    val length = value.logEntry.length
    val source = value.source

    LogEntry(0, entry, ingested, processed, length, source, charCount)
  }
}
