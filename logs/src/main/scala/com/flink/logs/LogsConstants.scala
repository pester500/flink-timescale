package com.flink.logs

trait LogsConstants {
  final val successQuery: String =
    """
      | INSERT INTO logs(
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
