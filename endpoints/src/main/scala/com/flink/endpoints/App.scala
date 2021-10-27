package com.flink.endpoints

import scalikejdbc.ConnectionPool

object App {
  def main(args: Array[String]) {

    Class.forName("org.h2.Driver")
    ConnectionPool.singleton("jdbc:h2:mem:hello", "user", "pass")

  }
}
