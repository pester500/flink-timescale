package com.flink.endpoints

import com.flink.logs.dto.LogEntry
import org.joda.time._
import scalikejdbc._
import scalikejdbc.WrappedResultSet

case class Member(id: Long, name: Option[String], createdAt: DateTime)


//object Member extends SQLSyntaxSupport[LogEntry] {
//  override val tableName = "members"
//  def apply(rs: WrappedResultSet) = new Member(
//    rs.long("id"),
//    rs.stringOpt("name"),
//    rs.jodaDateTime("created_at")
//  )
//}

// find all members
//val members: List[Member] =
//  sql"select * from members".map(rs => Member(rs)).list.apply()