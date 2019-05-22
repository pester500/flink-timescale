package com.flink.timescale

trait Constants {
  final val BOOTSTRAP_SERVERS: String = "bootstrap.servers"
  final val GROUP_ID: String = "group.id"
  final val AUTO_OFFSET_RESET: String = "auto.offset.reset"
  final val LATEST: String = "latest"
  final val query: String =
    """
      | INSERT INTO words(word, created_at) VALUES (?, ?)
    """.stripMargin
}
