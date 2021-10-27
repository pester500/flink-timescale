package com.flink.logs

trait LogsConstants {
  final val logTableName = "logs"

  final val successQuery: String =
    s"""
      | INSERT INTO $logTableName(
      |   id,
      |   line,
      |   ingested,
      |   processed,
      |   length,
      |   source,
      |   character_map
      | )
      | VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)
    """.stripMargin
}
