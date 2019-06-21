package com.flink.timescale.operators

import java.time.{Instant, LocalDateTime, ZoneOffset}

import com.flink.timescale.dto.WordMessage
import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.types.Row

class RowMapper extends MapFunction[WordMessage, Row] with Serializable {

  override def map(value: WordMessage): Row = {
    val row = new Row(23)
    row.setField(0, value.id)
    row.setField(1, value.caseNumber)
    row.setField(2, value.date)
    row.setField(3, value.block)
    row.setField(4, value.iucr)
    row.setField(5, value.primaryType)
    row.setField(6, value.description)
    row.setField(7, value.locationDescription)
    row.setField(8, value.arrest)
    row.setField(9, value.domestic)
    row.setField(10, value.beat)
    row.setField(11, value.district)
    row.setField(12, value.ward)
    row.setField(13, value.communityArea)
    row.setField(14, value.fbiCode)
    row.setField(15, value.xCoordinate)
    row.setField(16, value.yCoordinate)
    row.setField(17, value.year)
    row.setField(18, value.updatedOn)
    row.setField(19, value.latitude)
    row.setField(20, value.longitude)
    row.setField(21, value.locationDescription)
    row.setField(22, LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC))

    row
  }
}
