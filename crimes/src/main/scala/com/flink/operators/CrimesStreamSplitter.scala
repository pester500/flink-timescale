package com.flink.operators

import java.lang

import com.flink.config.Constants
import com.flink.dto.CrimeMessage
import org.apache.flink.streaming.api.collector.selector.OutputSelector

import scala.collection.JavaConverters._

class CrimesStreamSplitter extends OutputSelector[Either[String, CrimeMessage]] with Constants {

  override def select(value: Either[String, CrimeMessage]): lang.Iterable[String] = {
    List(if (value.isRight) PARSED else NOT_PARSED).asJava
  }

}
