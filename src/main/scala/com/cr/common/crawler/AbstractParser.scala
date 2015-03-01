package com.cr.common.crawler

import org.apache.commons.io.IOUtils
import org.apache.http.HttpHost
import org.apache.http.client.config.{CookieSpecs, RequestConfig}
import org.apache.http.client.methods.{CloseableHttpResponse, HttpUriRequest, RequestBuilder}
import org.apache.http.impl.client.{BasicCookieStore, HttpClientBuilder, HttpClients}
import org.apache.http.util.EntityUtils

import scala.collection.mutable

/**
 * conflict with logger in Logging
 * Created by caorong on 14-12-23 - 下午10:42.
 */
class AbstractParser extends JAbstractParser {

  //  val logger = LoggerFactory.getLogger(this.getClass)

  def responseCookie(url: String): mutable.Map[String, String] = {
    val httpclient = HttpClients.createDefault();
    val httpget = RequestBuilder.get().setUri(url).setConfig(requestConfig().build())

    httpget.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36")

    var response: CloseableHttpResponse = null
    var cookies: mutable.HashMap[String, String] = null
    try {
      //      val response = httpclient.execute(httpget.build());
      response = httpclient.execute(httpget.build())

      cookies = mutable.HashMap[String, String]()
      for (i <- response.getAllHeaders() if i.getName.equals("Set-Cookie")) yield {
        i.getValue.split(";").foreach((km: String) => {
          km.trim.split("=") match {
            case rr: Array[String] if rr.length == 2 => cookies.put(rr(0), rr(1))
            case _ => {}
          }
        })
      }
    } catch {
      case e: Exception => logger.error("get cookie error !!!", e)
    } finally {
      if (response != null) {
        EntityUtils.consume(response.getEntity())
      }
    }
    cookies
  }

  def requestConfig(proxy: HttpHost = null): RequestConfig.Builder = {
    val rb = RequestConfig.custom.
      setConnectionRequestTimeout(10000).setConnectTimeout(10000)
      .setSocketTimeout(10000).setCookieSpec(CookieSpecs.BEST_MATCH)
    //      .setProxy(proxy)
    if (proxy != null) {
      rb.setProxy(proxy)
    }
    rb
  }

  def requestPageCookies(request: HttpUriRequest): mutable.Map[String, String] = {
    val httpclient = HttpClients.createDefault();
    var repage: String = null
    var response: CloseableHttpResponse = null

    var cookies: mutable.HashMap[String, String] = null
    try {
      response = httpclient.execute(request);
      //      response.getAllHeaders
      //      repage = IOUtils.toString(response.getEntity().getContent, "utf-8")
      cookies = mutable.HashMap[String, String]()
      for (i <- response.getAllHeaders() if i.getName.equals("Set-Cookie")) yield {
        i.getValue.split(";").foreach((km: String) => {
          km.trim.split("=") match {
            case rr: Array[String] if rr.length == 2 => cookies.put(rr(0), rr(1))
            case _ => {}
          }
        })
      }
    } catch {
      case e: Exception => logger.error("")
    } finally {
      EntityUtils.consume(response.getEntity())
    }
    // 如此返回有问题  = =
    cookies
  }

  def requestPageInner(request: HttpUriRequest, cookieStore: BasicCookieStore = null): String = {
    val httpclient = cookieStore match {
      case null => HttpClients.createDefault()
      case _ => HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build()
    }
    var repage: String = null
    var response: CloseableHttpResponse = null
    try {
      response = httpclient.execute(request);
      repage = IOUtils.toString(response.getEntity().getContent, "utf-8")
      //      repage = IOUtils.toString(response.getEntity().getContent, "gb2312")
    } catch {
      case e: Exception => logger.error("")
    } finally {
      EntityUtils.consume(response.getEntity())
    }
    repage
  }

  def responsePageWithCookie(url: String, cookieStore: BasicCookieStore, headers: Map[String, String] = null): String = {
    val httpget = RequestBuilder.get().setUri(url).setConfig(requestConfig().build())
    httpget.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36")

    headers match {
      case m: Map[String, String] => m.foreach((e: (String, String)) => httpget.addHeader(e._1, e._2))
      case _ =>
    }

    //    val cookieStore = new BasicCookieStore();
    //    val cookie = new BasicClientCookie("JSESSIONID", "1234");
    //    cookie.setDomain(".github.com");
    //    cookie.setPath("/");
    //    cookieStore.addCookie(cookie);

    requestPageInner(httpget.build(), cookieStore)
  }

  def post(url: String): String = {
    val hpost = RequestBuilder.post().setUri(url).setConfig(requestConfig().build())
    requestPageInner(hpost.build())
  }

  def responsePage(url: String, headers: Map[String, String] = null): String = {
    //    https://geekpics.net/cBI0
    //    val httpclient = HttpClients.createDefault();
    val httpget = RequestBuilder.get().setUri(url).setConfig(requestConfig().build())
    httpget.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36")

    headers match {
      case m: Map[String, String] => m.foreach((e: (String, String)) => httpget.addHeader(e._1, e._2))
      case _ =>
    }

    requestPageInner(httpget.build())
    //    var response: CloseableHttpResponse = null
    //    try {
    //      response = httpclient.execute(httpget.build());
    //      return IOUtils.toString(response.getEntity().getContent, "utf-8")
    //    } catch {
    //      case e: Exception => logger.error(s"req page with url [${url}}] with header [${headers}] error !!!", e)
    //    } finally {
    //      if (response != null) {
    //        EntityUtils.consume(response.getEntity())
    //      }
    //    }
    //    null
  }
}
