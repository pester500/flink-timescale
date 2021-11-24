package com.flink.logs

import com.flink.logs.dto.ZipUtils.unzipString

import java.sql.Timestamp
import java.time.Instant
import com.flink.logs.dto.{KafkaLogMessage, LogEntry}
import org.apache.flink.api.common.functions.MapFunction

object LogMapper extends MapFunction[KafkaLogMessage, LogEntry] with Serializable {

  override def map(value: KafkaLogMessage): LogEntry = {
    val distinctCharacters = value.logEntry.toSet
    val charCount = distinctCharacters.map(char => (char.toString, value.logEntry.count(_ == char))).toMap
    val entry = new String(unzipString(value.logEntry))
    val ingested = value.timeIngested
    val processed = Timestamp.from(Instant.now())
    val length = value.logEntry.length
    val source = value.source

    LogEntry(0, entry, ingested, processed, length, source, charCount)
  }
}
