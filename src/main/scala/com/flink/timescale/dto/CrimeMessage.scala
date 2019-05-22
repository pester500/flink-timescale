package com.flink.timescale.dto

import java.sql.Timestamp

final case class CrimeMessage(
   id: Int,
   caseNumber: String,
   date: Timestamp,
   block: String,
   iucr: String,
   primaryType: String,
   description: String,
   locationDescription: String,
   arrest: Boolean,
   domestic: Boolean,
   beat: String,
   district: String,
   ward: Int,
   communityArea: String,
   fbiCode: String,
   xCoordinate: Double,
   yCoordinate: Double,
   year: Int,
   updatedOn: Timestamp,
   latitude: Double,
   longitude: Double,
   location: String
)
