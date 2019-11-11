package com.flink.timescale

trait Constants {
  final val AUTO_OFFSET_RESET: String = "auto.offset.reset"
  final val BOOTSTRAP_SERVERS: String = "bootstrap.servers"
  final val EARLIEST: String = "earliest"
  final val GROUP_ID: String = "group.id"
  final val LATEST: String = "latest"
  final val NOT_PARSED: String = "notParsed"
  final val NULL_FROM_KAFKA: String = "wat"
  final val PARSED: String = "parsed"
  final val POSTGRES_DRIVER: String = "org.postgresql.Driver"

  final val successQuery: String =
    """
      | INSERT INTO crimes(
      |   id,
      |   case_number,
      |   date,
      |   block,
      |   iucr,
      |   primary_type,
      |   description,
      |   location_description,
      |   arrest,
      |   domestic,
      |   beat,
      |   district,
      |   ward,
      |   community_area,
      |   fbi_code,
      |   x_coordinate,
      |   y_coordinate,
      |   year,
      |   updated_on,
      |   latitude,
      |   longitude,
      |   location,
      |   created_at)
      | VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """.stripMargin

  final val failQuery: String =
    """
      | INSERT INTO failures(
      |   failure,
      |   created_at)
      | VALUES (?, ?)
      |""".stripMargin

  final val processingSpeedQuery: String =
    """
      | INSERT INTO processing_speed(
      |   district,
      |   count,
      |   window_begin
      | )
      | VALUES (?, ?, ?)
      |""".stripMargin
}
