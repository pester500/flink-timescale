package com.flink.timescale.operators

import java.time.{Instant, LocalDateTime, ZoneOffset}

import com.flink.timescale.DateFormatter
import com.flink.timescale.dto.WordMessage
import com.github.tototoshi.csv.{CSVParser, DefaultCSVFormat}
import grizzled.slf4j.Logging
import org.apache.flink.api.common.functions.MapFunction

import scala.util.{Failure, Success, Try}

class WordMessageMapper extends MapFunction[String, WordMessage] with DateFormatter with Serializable with Logging {

  private lazy val parser = new CSVParser(new DefaultCSVFormat {})

  override def map(value: String): WordMessage = {

    val parsedCrime: List[String] = parser.parseLine(value).get
    logger.error(parsedCrime)

    val xCoord: Float = Try(parsedCrime(15).toFloat).getOrElse(0.0f)
    val yCoord: Float = Try(parsedCrime(16).toFloat).getOrElse(0.0f)

    Try(WordMessage(
      parsedCrime(0).toInt,
      parsedCrime(1),
      parseDate(parsedCrime(2)),
      parsedCrime(3),
      parsedCrime(4),
      parsedCrime(5),
      parsedCrime(6),
      parsedCrime(7),
      parsedCrime(8).toBoolean,
      parsedCrime(9).toBoolean,
      parsedCrime(10),
      parsedCrime(11),
      Try(parsedCrime(12).toInt).getOrElse(-1),
      parsedCrime(13),
      parsedCrime(14),
      xCoord,
      yCoord,
      parsedCrime(17).toInt,
      parseDate(parsedCrime(18)),
      Try(parsedCrime(19).toFloat).getOrElse(0.0f),
      Try(parsedCrime(20).toFloat).getOrElse(0.0f),
      parsedCrime(21)
    )) match {
      case Success(s) => s: WordMessage
      case Failure(fail) =>
        logger.error(s"Could not parse: $parsedCrime")
        logger.error(s"Exception: $fail")
        null
    }
  }
}