//package com.cr.common.redis
//
//import java.lang.reflect.{Method, InvocationHandler}
//import java.util
//
//import org.apache.commons.pool2.impl.GenericObjectPoolConfig
//import org.slf4j.LoggerFactory
//import redis.clients.jedis.{JedisPool, BasicCommands, JedisCommands, Jedis}
//import redis.clients.jedis.exceptions.JedisConnectionException
//
///**
// * <dependency>
//            <groupId>redis.clients</groupId>
//            <artifactId>jedis</artifactId>
//            <version>2.6.0</version>
//        </dependency>
// * Created by caorong on 15-3-1 - 下午4:23.
// */
//object Redis {
//
//  val logger = LoggerFactory.getLogger(Redis.getClass)
//
//  // copy from https://github.com/pk11/sedis/blob/master/src/main/scala/sedis.scala#L100
//  // if connection broken will return broken for one time
//  def withJedis[T](body: Jedis => T): T = {
//    val jedis: Jedis = RedisInner.pool.getResource
//    try {
//      body(jedis)
//    }
//    catch {
//      case e: JedisConnectionException => {
//        logger.error(e.getMessage, e)
//        RedisInner.pool.returnBrokenResource(jedis)
//        val jedis2: Jedis = RedisInner.pool.getResource
//        body(jedis2)
//      }
//    }
//    finally {
//      //      println(s"return ${jedis}")
//      RedisInner.pool.returnResourceObject(jedis)
//    }
//  }
//
//  trait JedisTotalCmd extends JedisCommands with BasicCommands
//
//  //  toArray(new Array[java.lang.String](0))
//  def jedis(): JedisCommands = {
//    val proxy: JedisCommands = java.lang.reflect.Proxy.newProxyInstance(
//      //      classOf[Redis].getClassLoader,
//      Redis.getClass.getClassLoader,
//      Array(classOf[JedisCommands], classOf[BasicCommands]),
//      new InvocationHandler {
//        override def invoke(proxy: Object, method: Method, args: Array[Object]): Object = {
//          //          val j = new GetJedisImpl().get()
//
//          val j = RedisInner.pool.getResource
//          println(s"get ${j},  --  ${util.Arrays.toString(args)}")
//          //          args.foreach(println)
//          try {
//            return method.invoke(j, args: _*)
//            //            val re = method.invoke(j)
//            //            println(s"ans => ${re}")
//            //            return re
//          } catch {
//            case e: Exception => {
//              logger.error(e.getMessage, e)
//              throw e.getCause
//              //              RedisInner.pool.returnBrokenResource(j)
//            }
//          } finally {
//            RedisInner.pool.returnResourceObject(j)
//          }
//          null
//        }
//      }
//    ).asInstanceOf[JedisCommands]
//    return proxy
//    //    MyInterface.class.getClassLoader(),
//    //    new Class[]{MyInterface.class},
//    //    handler);
//
//    //    RedisInner.pool.getResource
//    //    RedisInner.pool.returnBrokenResource()
//    //    RedisInner.pool.returnResource()
//  }
//
//  def main(args: Array[String]): Unit = {
//    println(jedis())
//    println(jedis().set("aa", "14"))
//    println(jedis().get("aa"))
//    //    println(jedis.get().set("aa", "1"))
//    withJedis { c =>
//      for (a <- 1 to 1000) {
//        //        println( "Value of a: " + a );
//        println(c.set("aa", "1"))
//      }
//    }
//  }
//}
//
//object RedisInner {
//  val pool = new JedisPool({
//    val pc = new GenericObjectPoolConfig()
//    pc.setTestWhileIdle(true);
//    pc.setMinEvictableIdleTimeMillis(60000);
//    pc.setTimeBetweenEvictionRunsMillis(30000);
//    pc.setNumTestsPerEvictionRun(-1);
//    pc.setMaxTotal(10)
//    pc.setMaxIdle(10)
//    pc.setMinIdle(5)
//    pc
//  }, "localhost", 6379, 10 * 1000, null, 1);
//}
