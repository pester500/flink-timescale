package com.flink.logs

class Application {

  def main(args: Array[String]): Unit = {
    val flow = new LogsFlow
    flow.execute()
  }
}
