package com.cr.common.crawler;

import com.cr.common.exception.FileTooLargeException;
import com.cr.common.exception.StorageFileNotFoundException;
import com.cr.common.log.Logging;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.utils.UrlUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by caorong on 14-12-1.
 */
public abstract class JAbstractParser {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected final String CHROME_V39 = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36";

    protected void download(Site site, String url, String savePath) {
        Set<Integer> acceptStatCode;
        String charset = null;
        Map<String, String> headers = null;
        if (site != null) {
            acceptStatCode = site.getAcceptStatCode();
            charset = site.getCharset();
            headers = site.getHeaders();
        } else {
            acceptStatCode = Sets.newHashSet(200);
        }
        logger.info("downloading url" + url);
        RequestBuilder requestBuilder = RequestBuilder.get().setUri(url);
        if (headers != null) {
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                requestBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setConnectionRequestTimeout(site.getTimeOut()).setConnectTimeout(site.getTimeOut())
                .setSocketTimeout(site.getTimeOut()).setCookieSpec(CookieSpecs.BEST_MATCH);
        if (site != null && site.getHttpProxy() != null) {
            requestConfigBuilder.setProxy(site.getHttpProxy());
        }
        requestBuilder.setConfig(requestConfigBuilder.build());
        CloseableHttpResponse httpResponse = null;

        HttpRequestBase request = null;
        try {
            httpResponse = getHttpClient(site).execute(requestBuilder.build());

        } catch (Exception e) {
            logger.error("download error", e);
        }

        final File file = new File(savePath);
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        InputStream inputStream = null;
        try {
            file.createNewFile();

            fileOutputStream = new FileOutputStream(file);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

            logger.debug("download status code => {}", httpResponse.getStatusLine().getStatusCode());
            if (httpResponse.getStatusLine().getStatusCode() == 404)
                throw new StorageFileNotFoundException("url: " + url, null);

            if (site.getAcceptStatCode().contains(
                    httpResponse.getStatusLine().getStatusCode())) {
                HttpEntity entity = httpResponse.getEntity();

                long contentLength = entity.getContentLength();
//                System.out.println(contentLength);

                // TODO MAKE MORE flexible
                // throw error if file more than 20MB
                if (contentLength > 20000000)
                    throw new FileTooLargeException("contentLength => " + contentLength, null);

                inputStream = entity.getContent();
                byte[] b = new byte[4096];
                int length = 0;
                int receiveLength = 0;
                while ((length = inputStream.read(b)) != -1) {
                    bufferedOutputStream.write(b, 0, length);
                    receiveLength += length;
                }
                bufferedOutputStream.flush();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (StorageFileNotFoundException e) {
            throw new StorageFileNotFoundException("url: " + url, e);
        } catch (FileTooLargeException e) {
            throw new FileTooLargeException(e.getMessage(), e);
        } finally {
            if (httpResponse != null) {
                try {
                    EntityUtils.consume(httpResponse.getEntity());
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            try {
                if (bufferedOutputStream != null)
                    bufferedOutputStream.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
//            try {
//                if (inputStream != null)
//                    inputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    protected String fetch(Site site, String url) {
        Set<Integer> acceptStatCode;
        String charset = null;
        Map<String, String> headers = null;
        if (site != null) {
            acceptStatCode = site.getAcceptStatCode();
            charset = site.getCharset();
            headers = site.getHeaders();
        } else {
            acceptStatCode = Sets.newHashSet(200);
        }
        logger.info("downloading page " + url);
        RequestBuilder requestBuilder = RequestBuilder.get().setUri(url);
        if (headers != null) {
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                requestBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setConnectionRequestTimeout(site.getTimeOut()).setConnectTimeout(site.getTimeOut())
                .setSocketTimeout(site.getTimeOut()).setCookieSpec(CookieSpecs.BEST_MATCH);
        if (site != null && site.getHttpProxy() != null) {
            requestConfigBuilder.setProxy(site.getHttpProxy());
        }
        requestBuilder.setConfig(requestConfigBuilder.build());
        CloseableHttpResponse httpResponse = null;

        try {
            httpResponse = getHttpClient(site).execute(requestBuilder.build());
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (acceptStatCode.contains(statusCode)) {
                // charset
                if (charset == null) {
                    String value = httpResponse.getEntity().getContentType().getValue();
//                    System.out.println("------------------------------");
//                    System.out.println("------------------------------");
//                    System.out.println("------------------------------");
//                    System.out.println( httpResponse.getEntity().getContentType());
//                    System.out.println( httpResponse.getEntity().getContentEncoding());
//                    System.out.println( httpResponse.getEntity().getContent());
//                    System.out.println("------------------------------");
//                    System.out.println("------------------------------");
//                    System.out.println("------------------------------");

                    charset = UrlUtils.getCharset(value);
                }
                // String html = IOUtils.toString(httpResponse.getEntity()
                // .getContent(), charset);
                // Document document = Jsoup.parse(html);
                // return Jsoup.parse(html);
                return IOUtils.toString(httpResponse.getEntity().getContent(), charset);
            } else {
                logger.warn("code error " + statusCode + "\t" + url);
                return null;
            }
        } catch (IOException e) {
            logger.warn("download page " + url + " error", e);
            return null;
        } catch (Exception e) {
            logger.warn("unknown exception," + url + " error", e);
            return null;
        } finally {
            try {
                if (httpResponse != null) {
                    // ensure the connection is released back to pool
                    EntityUtils.consume(httpResponse.getEntity());
                }
            } catch (IOException e) {
                logger.warn("close response fail", e);
            }
        }
    }

    private final Map<String, CloseableHttpClient> httpClients = new HashMap<String, CloseableHttpClient>();

    private MyHttpClientGenerator httpClientGenerator = new MyHttpClientGenerator();

    private CloseableHttpClient getHttpClient(Site site) {
        if (site == null) {
            return httpClientGenerator.getClient(null);
        }
//        String domain = site.getDomain();
//        CloseableHttpClient httpClient = httpClients.get(domain);
//        if (httpClient == null) {
//            synchronized (this) {
//                if (httpClient == null) {
//                    httpClient = httpClientGenerator.getClient(site);
        CloseableHttpClient httpClient = httpClientGenerator.getClient(site);
//                    httpClients.put(domain, httpClient);
//                }
//            }
//        }
        return httpClient;
    }
}
