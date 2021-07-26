package com.flink.yahoofinance.yahoofinance.histquotes

import java.math.BigDecimal
import java.util.Calendar

case class HistoricalQuote(
  private val symbol: String,
  private var date: Calendar,
  private val open: BigDecimal,
  private val low: BigDecimal,
  private val high: BigDecimal,
  private val close: BigDecimal,
  private val adjClose: BigDecimal,
  private val volume: Long
)
