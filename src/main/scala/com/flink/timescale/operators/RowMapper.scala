package com.flink.timescale.operators

import com.flink.timescale.dto.WordMessage
import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.types.Row

class RowMapper extends MapFunction[WordMessage, Row]{

  override def map(value: WordMessage): Row = {
    val row = new Row(2)
    row.setField(0, value.word)
    row.setField(1, value.createdAt)
    row
  }
}
