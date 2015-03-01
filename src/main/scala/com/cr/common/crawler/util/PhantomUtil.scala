package com.cr.common.crawler.util

import scala.sys.process._
import scala.util.Properties

/**
 * Created by caorong on 15-2-6 - 下午11:51.
 */
trait PhantomUtil {

  val PHANTOM_PATH = Properties.scalaPropOrElse("PIVIX_PATH", "phantomjs")

  val JS_PATH = Properties.scalaPropOrElse("JS_PATH", "xxx.js")

  def reqWithPhantomWithProxy(): String = {
    //    val dirContents = "ls".!! //s"${Conf.PHANTOM_PATH} ${Conf.JS_PATH}".!!
    val dirContents = s"${PHANTOM_PATH} --proxy=http://${PROXY} ${JS_PATH}".!!
    return dirContents.trim
  }

  def reqWithPhantom(js: String, args: String): String = {
    val dirContents = s"${PHANTOM_PATH} ${js} ${args}".!!
    return dirContents.trim
  }

  def PROXY(): String = {
    ""
  }
}

object testPhantom extends PhantomUtil {

  def main(args: Array[String]): Unit = {
    println("====")
    println(reqWithPhantomWithProxy())
    println("====")
  }

  override def PROXY(): String = {
    "183.207.228.8:80"
  }
}
