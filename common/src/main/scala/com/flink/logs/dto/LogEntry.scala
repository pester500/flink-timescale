package com.flink.logs.dto

import java.sql.Timestamp

final case class LogEntry(
  id: Long,
  line: String,
  ingested: Timestamp,
  processed: Timestamp,
  length: Int,
  source: String,
  letterCount: Map[String, Int]
)