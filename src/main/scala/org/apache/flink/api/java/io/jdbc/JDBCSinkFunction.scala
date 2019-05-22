package org.apache.flink.api.java.io.jdbc

import grizzled.slf4j.Logging
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import org.apache.flink.types.Row

/**
  * This class is its own package because of some weird dependency resolution issue
  */
class JDBCSinkFunction(outputFormat: JDBCOutputFormat) extends RichSinkFunction[Row] with Logging {

  override def invoke(row: Row): Unit = {
    outputFormat.writeRecord(row)
    outputFormat.flush()
  }

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    val ctx = getRuntimeContext
    outputFormat.setRuntimeContext(ctx)
    outputFormat.open(ctx.getIndexOfThisSubtask, ctx.getNumberOfParallelSubtasks)
  }

  override def close(): Unit = {
    outputFormat.close()
    super.close()
  }
}
