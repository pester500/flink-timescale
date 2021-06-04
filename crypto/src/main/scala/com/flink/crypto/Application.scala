package com.flink.crypto

object Application extends Serializable {

  def main(args: Array[String]): Unit = {
    val flow = new CryptoFlow
    flow.execute()
  }
}
