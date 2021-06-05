package com.flink.crypto

import grizzled.slf4j.Logging
import org.apache.commons.compress.compressors.CompressorStreamFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import sttp.client3._

import java.io.{BufferedInputStream, BufferedReader, FileInputStream, InputStreamReader}
import java.util.Properties

object SeedKafka extends Logging {

  val url = "https://min-api.cryptocompare.com/data/all/coinlist"

  private val producer = new KafkaProducer[String, String](kafkaProperties)

  def main(args: Array[String]): Unit = {
    val request = basicRequest.get(uri"https://api.github.com/search/repositories?q=$query&sort=$sort")
    val backend = HttpURLConnectionBackend()
    val response = request.send(backend)

  }

  def getBufferedReaderForCompressedFile: BufferedReader = {
    val fin = new FileInputStream("/home/george/workspace/flink-timescale/crimes.bz2")
    val bis = new BufferedInputStream(fin)
    val input = new CompressorStreamFactory().createCompressorInputStream(bis)
    val br2 = new BufferedReader(new InputStreamReader(input))
    br2
  }

  private def kafkaProperties: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", "192.168.2.67:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("acks", "0")
    props.put("compression.type", "snappy")

    props
  }
}
