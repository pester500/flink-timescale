package com.flink.logs.dto

import org.apache.commons.compress.compressors.gzip.{GzipCompressorInputStream, GzipCompressorOutputStream}
import org.apache.commons.io.output.ByteArrayOutputStream

import java.io.ByteArrayInputStream
import java.sql.Timestamp
import java.util

final case class KafkaLogMessage(
  key: String,
  logEntry: Array[Byte],
  timeIngested: Timestamp,
  source: String
)

object ZipUtils {

  def autoClose[A <: AutoCloseable, B](resource: A)(code: A => B): B = {
    try code(resource)
    finally resource.close()
  }

  def zipString(src: String, charSetName: String = "UTF-8"): Array[Byte] = {
    autoClose(new ByteArrayOutputStream) { arrayOutputStream =>
      autoClose(new GzipCompressorOutputStream(arrayOutputStream)) { outputStream =>
        outputStream.write(src.getBytes(charSetName))
      }
      arrayOutputStream.toByteArray
    }
  }

  def unzipString(src: Array[Byte], bufferSize: Int = 256): Array[Byte] = {
    val readBuffer = new Array[Byte](bufferSize)
    autoClose(new GzipCompressorInputStream(new ByteArrayInputStream(src))) { inputStream =>
      val read = inputStream.read(readBuffer, 0, readBuffer.length)
      util.Arrays.copyOf(readBuffer, read)
    }
  }
}

