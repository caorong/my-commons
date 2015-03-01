package com.cr.common.log

import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by caorong on 15-2-26 - 下午10:22.
  */
trait Logging {
   @transient lazy val logger: Logger = LoggerFactory.getLogger(getClass)
 }
