package com.flink.logs

import com.flink.logs.dto.ZipUtils.zipString

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

import scala.compat.java8.StreamConverters._

object SeedKafka extends Logging {

  private implicit val formats: Formats = Serialization.formats(NoTypeHints)

  private var i = 0

  private val producer = new KafkaProducer[String, String](kafkaProperties)

  def main(args: Array[String]): Unit = {
    getFileTree(new File("/home/george/Downloads/logs")).filter(_.isFile()).foreach(file => {
      val compressedList = time {
        getBufferedReader(file).lines().toScala[List].par.map(line => { zipString(line.replaceAll("\u0000", "")) })
      }
      compressedList.par.map(line => {
        val message = KafkaLogMessage(UUID.randomUUID().toString, line, Timestamp.from(Instant.now()), file.getAbsolutePath)
        val record = new ProducerRecord[String, String]("logs", write(message))
        if (i % 100000 == 0) {
          println(s"$i, message: $message")
        }
        i += 1
        producer.send(record)
      })

    })
    producer.close()
  }

  def getBufferedReader(file: File): BufferedReader = {
    val fin = new FileInputStream(file)
    val bis = new BufferedInputStream(fin)
    new BufferedReader(new InputStreamReader(bis))
  }

  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
    else Stream.empty)

  private def kafkaProperties: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", "192.168.2.246:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("acks", "0")
    props.put("compression.type", "snappy")

    props
  }

  def time[R](block: => R): R = {
    val t0 = System.currentTimeMillis()
    val result = block    // call-by-name
    val t1 = System.currentTimeMillis()
//    println("Elapsed time: " + (t1 - t0) + "ms")
    result
  }

}
