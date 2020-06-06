package com.flink.timescale.operators

import java.sql.Timestamp
import java.time.Instant

import com.flink.timescale.dto.CrimeMessage
import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.types.Row

class SuccessRowMapper extends MapFunction[Either[String, CrimeMessage], Row] with Serializable {

  override def map(value: Either[String, CrimeMessage]): Row = {
    val crime: CrimeMessage = value.right.get

    val row = new Row(23)
    row.setField(0, crime.id)
    row.setField(1, crime.caseNumber)
    row.setField(2, crime.date)
    row.setField(3, crime.block)
    row.setField(4, crime.iucr)
    row.setField(5, crime.primaryType)
    row.setField(6, crime.description)
    row.setField(7, crime.locationDescription)
    row.setField(8, crime.arrest)
    row.setField(9, crime.domestic)
    row.setField(10, crime.beat)
    row.setField(11, crime.district)
    row.setField(12, crime.ward)
    row.setField(13, crime.communityArea)
    row.setField(14, crime.fbiCode)
    row.setField(15, crime.xCoordinate)
    row.setField(16, crime.yCoordinate)
    row.setField(17, crime.year)
    row.setField(18, crime.updatedOn)
    row.setField(19, crime.latitude)
    row.setField(20, crime.longitude)
    row.setField(21, crime.locationDescription)
    row.setField(22, Timestamp.from(Instant.now()))

    row
  }
}
