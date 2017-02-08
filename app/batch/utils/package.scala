package batch

import java.io.File

import config.Settings

import scala.io.Source


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

}


object TestApp extends App {

  val data = utils.loadCVSSourceFile(new File("/Users/fsznajderman/dev/projects/devoxx/data/data.csv"))
  println(data(0))
  println(data(0).get(" RegId"))
  println(data(0).values)


}
