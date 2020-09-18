package com.flink.logs

import com.flink.logs.dto.LogEntry
import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.types.Row
import org.json4s.{Formats, NoTypeHints}
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write

object LogRowMapper extends MapFunction[LogEntry, Row] with Serializable {

  private implicit lazy val formats: Formats = Serialization.formats(NoTypeHints)

  override def map(value: LogEntry): Row = {
    val row = new Row(6)
    row.setField(0, value.line)
    row.setField(1, value.ingested)
    row.setField(2, value.processed)
    row.setField(3, value.length)
    row.setField(4, value.source)
    row.setField(5, write(value.letterCount))

    row
  }
}
