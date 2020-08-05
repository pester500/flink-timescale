package com.flink.timescale

import org.apache.commons.compress.compressors.CompressorStreamFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.io.{BufferedInputStream, BufferedReader, FileInputStream, InputStreamReader}
import java.util.Properties

import grizzled.slf4j.Logging

object SeedKafka extends Logging {

  private var i = 0

  private val producer = new KafkaProducer[String, String](kafkaProperties)

  def main(args: Array[String]): Unit = {
    for (x <- 1 until 10 ) {
      getBufferedReaderForCompressedFile.lines().forEach(line => {
        val record = new ProducerRecord[String, String]("input", line)
        if (i % 10000 == 0) {
          println(s"$x, $i, line: $line")
        }
        producer.send(record)
        i += 1
      })
    }
    producer.close()
  }

  def getBufferedReaderForCompressedFile: BufferedReader = {
    val fin = new FileInputStream("./crimes.bz2")
    val bis = new BufferedInputStream(fin)
    val input = new CompressorStreamFactory().createCompressorInputStream(bis)
    val br2 = new BufferedReader(new InputStreamReader(input))
    br2
  }

  private def kafkaProperties: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("acks", "0")

    props
  }
}