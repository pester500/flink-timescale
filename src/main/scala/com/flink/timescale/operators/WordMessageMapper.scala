package com.flink.timescale.operators

import java.time.{Instant, LocalDateTime, ZoneOffset}

import com.flink.timescale.dto.WordMessage
import org.apache.flink.api.common.functions.MapFunction

class WordMessageMapper extends MapFunction[String, WordMessage] with Serializable {

  override def map(value: String): WordMessage = {
    WordMessage(value, LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC))
  }
}
