package com.flink

trait CrimesConstants {
  final val crimesTable: String = "crimes"

  final val successQuery: String =
    s"""
      | INSERT INTO $crimesTable(
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

}
