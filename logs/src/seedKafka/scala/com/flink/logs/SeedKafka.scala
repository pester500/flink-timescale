package com.flink.logs

import java.io.{BufferedInputStream, BufferedReader, File, FileInputStream, InputStreamReader}
import java.sql.Timestamp
import java.time.Instant
import java.util.{Properties, UUID}

import com.flink.logs.dto.KafkaLogMessage
import grizzled.slf4j.Logging
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.json4s.{Formats, NoTypeHints}
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write

object SeedKafka extends Logging {

  private implicit val formats: Formats = Serialization.formats(NoTypeHints)

  private var i = 0

  private val producer = new KafkaProducer[String, String](kafkaProperties)

  def main(args: Array[String]): Unit = {
    getFileTree(new File("/home/george/Downloads/peapod/logs")).filter(_.isFile()).foreach(file => {
      getBufferedReader(file).lines().forEach(line => {
        val message = KafkaLogMessage(UUID.randomUUID().toString, line.replaceAll("\u0000", ""), Timestamp.from(Instant.now()), file.getAbsolutePath)
        val record = new ProducerRecord[String, String]("logs", write(message))
        if (i % 100000 == 0) {
          println(s"$i, message: $message")
        }
        producer.send(record)
        i += 1
      })
    })
    producer.close()
  }

  def getBufferedReader(file: File): BufferedReader = {
    val fin = new FileInputStream(file)
    val bis = new BufferedInputStream(fin)
    val br = new BufferedReader(new InputStreamReader(bis))
    br
  }

  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
    else Stream.empty)

  private def kafkaProperties: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("acks", "0")
    props.put("compression.type", "snappy")

    props
  }
}
