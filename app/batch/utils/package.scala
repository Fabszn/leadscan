package batch

import java.io.{File, FileReader}

import com.opencsv.CSVReader
import config.Settings
import org.apache.commons.lang3.StringUtils

import scala.io.Source
import scala.collection.JavaConverters

/**
  * Created by fsznajderman on 07/02/2017.
  */
package object utils {

  def loadCVSSourceFile(f: File): Seq[Map[String, String]] = {

    def convert = (elems: Seq[String], sep: String) => {
      val headers = (elems.head split sep).toList
      elems.tail map (l => (headers zip (l split sep).toList).toMap)
    }

    convert((Source.fromFile(f) getLines()).toSeq, ",")
  }

  def loadCSVSourceFileWithLib(csvFile: File): Seq[Map[String, String]] = {
    val it = new CSVReader(new FileReader(csvFile), ',', '"').iterator()
    val lines: List[Array[String]] = JavaConverters.asScalaIteratorConverter(it).asScala.toList
    lines.headOption.map(_.toList) match {
      case None => Seq.empty
      case Some(headers) => {
        println("CSV Header " + headers) // TODO voir comment utiliser tes loggers
        lines.tail.map {
          oneLine =>
            (headers zip oneLine.map(s=>StringUtils.trimToEmpty(s)).toList).toMap
        }
      }
    }
  }

}

