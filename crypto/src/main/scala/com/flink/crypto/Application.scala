package com.flink.crypto

object Application extends Serializable {

  def main(args: Array[String]): Unit = {
    val flow = new LogsFlow
    flow.execute()
  }
}
