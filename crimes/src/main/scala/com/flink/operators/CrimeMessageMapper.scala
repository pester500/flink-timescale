package com.flink.operators

import java.sql.Timestamp
import java.time.Instant

import com.flink.DateFormatter
import com.flink.dto.CrimeMessage
import com.github.tototoshi.csv.{CSVParser, DefaultCSVFormat}
import grizzled.slf4j.Logging
import org.apache.flink.api.common.functions.MapFunction

import scala.util.{Failure, Success, Try}

class CrimeMessageMapper extends MapFunction[String, Either[String, CrimeMessage]] with DateFormatter with Serializable with Logging {

  private lazy val parser = new CSVParser(new DefaultCSVFormat {})

  override def map(value: String): Either[String, CrimeMessage] = {

    val parsedCrime: List[String] = Try {
      parser.parseLine(value).getOrElse(Nil).map(_.replaceAll("\u0000", ""))
    }.getOrElse(Nil)
    
    Try(CrimeMessage(
      parsedCrime.head.toInt,
      parsedCrime(1),
      Try(parseDate(parsedCrime(2))).getOrElse(Timestamp.from(Instant.now)),
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
      Try(parsedCrime(15).toDouble).getOrElse(0.0d),
      Try(parsedCrime(16).toDouble).getOrElse(0.0d),
      parsedCrime(17).toInt,
      parseDate(parsedCrime(18)),
      Try(parsedCrime(19).toDouble).getOrElse(0.0d),
      Try(parsedCrime(20).toDouble).getOrElse(0.0d),
      parsedCrime(21)
    )) match {
      case Success(s) => Right(s: CrimeMessage)
      case Failure(fail) =>
        logger.error(s"Could not parse: $value", fail)
        Left(value: String)
    }
  }
}