package com.flink

object Application extends Serializable {

  def main(args: Array[String]): Unit = {
    val timescaleFlow = new TimescaleFlow
    timescaleFlow.execute()
  }
}
