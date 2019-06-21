package com.flink.timescale.dto

import java.time.LocalDateTime

final case class WordMessage(
  id: Int,
  caseNumber: String,
  date: LocalDateTime,
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
  xCoordinate: Float,
  yCoordinate: Float,
  year: Int,
  updatedOn: LocalDateTime,
  latitude: Float,
  longitude: Float,
  location: String
)
