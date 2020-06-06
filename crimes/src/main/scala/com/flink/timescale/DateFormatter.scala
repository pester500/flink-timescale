package com.flink.timescale

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

trait DateFormatter {

  def time[R](block: => R, operation: String): R = {
    val t0 = System.currentTimeMillis()
    val result = block    // call-by-name
    val t1 = System.currentTimeMillis()
    println(s"$operation: elapsed time: ${t1 - t0} ms")
    result
  }

  lazy val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a")

  def parseDate(date: String): Timestamp = {
    Timestamp.valueOf(LocalDateTime.parse(date, formatter))
  }
}