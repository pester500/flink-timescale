package com.flink.timescale.operators

import com.flink.timescale.dto.CrimeMessage
import org.apache.flink.api.common.functions.AggregateFunction

object WindowedDistrictCrimeAggregator extends AggregateFunction[CrimeMessage, Seq[CrimeMessage], Int] {
  override def createAccumulator(): Seq[CrimeMessage] = Nil

  override def add(value: CrimeMessage, accumulator: Seq[CrimeMessage]): Seq[CrimeMessage] = accumulator :+ value

  override def getResult(accumulator: Seq[CrimeMessage]): Int = accumulator.size

  override def merge(a: Seq[CrimeMessage], b: Seq[CrimeMessage]): Seq[CrimeMessage] = a ++ b
}
