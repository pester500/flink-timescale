package com.flink.operators

import java.sql.Timestamp
import java.time.Instant

import com.flink.dto.CrimeMessage
import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.types.Row

class FailureRowMapper extends MapFunction[Either[String, CrimeMessage], Row] with Serializable {

  override def map(value: Either[String, CrimeMessage]): Row = {
    val failure: String = value.left.get

    val row = new Row(2)
    row.setField(0, failure)
    row.setField(1, Timestamp.from(Instant.now()))

    row
  }
}
