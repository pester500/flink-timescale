package com.flink.timescale.dto

import java.time.LocalDateTime

final case class WordMessage(
  word: String,
  createdAt: LocalDateTime
)