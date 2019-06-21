package com.flink.timescale

trait Constants {
  final val BOOTSTRAP_SERVERS: String = "bootstrap.servers"
  final val GROUP_ID: String = "group.id"
  final val AUTO_OFFSET_RESET: String = "auto.offset.reset"
  final val LATEST: String = "latest"
  final val query: String =
    """
      | INSERT INTO crimes(id,
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
      |   VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """.stripMargin
}
