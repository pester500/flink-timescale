package org.apache.flink.api.java.io.jdbc

import org.apache.flink.configuration.Configuration
import org.apache.flink.connector.jdbc.JdbcOutputFormat
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import org.apache.flink.types.Row

class JDBCSinkFunction(val outputFormat: JdbcOutputFormat) extends RichSinkFunction[Row] with CheckpointedFunction with Serializable {

  override def invoke(value: Row): Unit = {
    outputFormat.writeRecord(value)
  }

  override def snapshotState(context: FunctionSnapshotContext): Unit = {
    outputFormat.flush()
  }

  override def initializeState(context: FunctionInitializationContext): Unit = {
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
