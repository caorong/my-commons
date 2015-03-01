//package com.cr.common.db
//
//import java.sql.Connection
//
///**
// * Created by caorong on 15-2-17.
// */
//trait TransactionSupport {
//  def withTransaction[T](body: Connection => T): T = {
//    val conn: Connection = DBUtils.cpds.getConnection
//    conn.setAutoCommit(false)
//    try {
//      val re = body(conn)
//      conn.commit()
//      re
//    } catch {
//      case e => {
//        conn.rollback()
//        throw e
//      }
//    } finally {
//      // 原样回pool  (每次close后 都会new一个新的conn, 所以无需自己还原)
//      //      conn.setAutoCommit(false)
//      // 这个close也是坑爹, 此close == return connection to pool
//      conn.close()
//    }
//  }
//}
