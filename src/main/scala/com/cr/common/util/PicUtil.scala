package com.cr.common.util

import java.awt.image.BufferedImage
import java.io.{File, FileOutputStream, OutputStream}
import java.util
import java.util.concurrent
import java.util.concurrent.{Callable, Executors}
import javax.imageio.stream.{ImageInputStream, ImageOutputStream, MemoryCacheImageOutputStream}
import javax.imageio._

import com.cr.common.log.Logging
import org.slf4j.LoggerFactory

import scala.util.Properties

/**
 *
 * http://www.tutorialspoint.com/java_dip/image_compression_technique.htm
 *
 * Created by caorong on 14-12-30 - 下午10:26.
 */
object PicUtil extends Logging {
  val CacheDirectory = Properties.scalaPropOrElse("DOWNLOAD_SIZE_PATH", "/tmp")

  def compress(bufferedImage: BufferedImage,
               outputStream: OutputStream,
               quality: Float): Unit = {
    var imageOutputStream: ImageOutputStream = null
    var imageWriter: ImageWriter = null
    try {
      val iterator: util.Iterator[ImageWriter] = ImageIO.getImageWritersByFormatName("jpg")
      imageWriter = iterator.next
      val imageWriteParam: ImageWriteParam = imageWriter.getDefaultWriteParam
      imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT)
      imageWriteParam.setCompressionQuality(quality)
      imageOutputStream = new MemoryCacheImageOutputStream(outputStream)
      imageWriter.setOutput(imageOutputStream)
      val iioimage: IIOImage = new IIOImage(bufferedImage, null, null)
      imageWriter.write(null, iioimage, imageWriteParam)
      imageOutputStream.flush
    } catch {
      case e: Exception => throw e
    } finally {
      if (imageOutputStream != null) {
        imageOutputStream.close
        imageOutputStream = null
      }
      if (imageWriter != null)
        imageWriter.dispose
    }
  }

  // compress recury until return path
  // quality from 1.0 minus  0.01 0.02 0.04 0.08...
  def compressRecu(sourcePath: String, maxSize: Long): String = {
    val picbase = sourcePath.split("\\.")(0)
    var i = 1
    var nquality = 0.01f
    while (true) {
      var os: OutputStream = null
      try {
        val compressedf = new File(s"${picbase}_${i}.jpg")
        val sfile = new File(sourcePath)
        if (sfile.length() < maxSize)
          return sfile.getAbsolutePath
        val quality = 1.0f - nquality
        os = new FileOutputStream(compressedf)
        ImageIO.setUseCache(true)
        ImageIO.setCacheDirectory(new File(CacheDirectory))
        compress(ImageIO.read(sfile),
                  os, quality)

        logger.info(
                     s"""compress no.${i}, quality => ${quality},
                        |sfile size => ${sfile.length()}
                        |Max size =>   ${maxSize}
                        |cfile size => ${compressedf.length()}""".stripMargin)
        compressedf match {
          case f if f.length() < maxSize => return f.getAbsolutePath
          case _ => compressedf.delete()
        }
      } catch {
        case e: Exception => logger.error("compress error !!!", e)
      } finally {
        if (os != null) {
          os.close()
        }
        if (nquality * 2 > 0.5) {
          nquality += 0.1f
        } else {
          nquality *= 2
        }
        i += 1
      }
    }
    null
  }

  // my vps only has one core
  val excuteor = Executors.newFixedThreadPool(1)

  // compress with thread pool
  def compressWithThread(sourcePath: String, maxSize: Long): concurrent.Future[String] = {
    excuteor.submit(new Callable[String] {
      override def call(): String = {
        try {
          compressRecu(sourcePath, maxSize)
        } catch {
          case e: Exception => {
            logger.error("compress error!!!", e)
            throw new RuntimeException("get compress pic error!!!")
          }
        } finally {
        }
      }
    })
  }


  // return width hight
  @Deprecated
  def picwh2(sourceFile: File): (Int, Int) = {
    logger.info(s"check width hight @ ${sourceFile.getAbsolutePath}")
    //    val image = ImageIO.read(sourceFile)
    val image: BufferedImage = ImageIO.read(sourceFile);
    (image.getWidth, image.getHeight)
  }


  def picwh(sourceFile: File): (Int, Int) = {
    logger.info(s"check width hight @ ${sourceFile.getAbsolutePath}")
    val input: ImageInputStream = ImageIO.createImageInputStream(sourceFile)

    try {
      // Get the reader
      val readers: util.Iterator[ImageReader] = ImageIO.getImageReaders(input);

      if (!readers.hasNext()) {
        throw new RuntimeException("No reader for: " + sourceFile.getAbsolutePath);
      } else {
        val reader: ImageReader = readers.next()
        try {
          reader.setInput(input)
          (reader.getWidth(0), reader.getHeight(0))
        } finally {
          reader.dispose()
        }
      }
    } catch {
      case e: Exception => throw e
    } finally {
      input.close()
    }
  }

  def main(args: Array[String]): Unit = {
    //    val re = compressRecu("/Users/caorong/Documents/workspace_scala/xxrestorer/74.jpg", 2 * 1000 * 1000)
    //    println(re)

    //    val r1 = compressWithThread("/Users/caorong/Documents/workspace_scala/xxrestorer/74.jpg", 10 * 1000 * 1000)
    //    println("call 1")
    //    println(r1.get)

    //    val r2 = compressWithThread("/Users/caorong/Documents/workspace_scala/xxrestorer/1986.jpg", 10 * 1000 * 1000)
    //    println("call 2")
    //    println(r2.get)

    //    println(picwh(new File("/Users/caorong/Documents/workspace_scala/xxrestorer/74.jpg")))
//    println(picwh(new File("/Users/caorong/Documents/workspace_scala/xxrestorer/30852.jpg")))
//    println(picwh(new File("/Users/caorong/Documents/workspace_scala/xxrestorer/weibo3187.jpg")))

    println(picwh2(new File("/Users/caorong/Documents/workspace_scala/xxrestorer/30852.jpg")))
    println(picwh2(new File("/Users/caorong/Documents/workspace_scala/xxrestorer/weibo3187.jpg")))
  }
}
