package com.flink.logs.dto

import java.sql.Timestamp
import java.util.UUID

final case class KafkaLogMessage(
  key: String,
  logEntry: String,
  timeIngested: Timestamp,
  source: String
)
