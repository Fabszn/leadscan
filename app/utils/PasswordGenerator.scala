package utils

/**
  * Created by fsznajderman on 10/03/2017.
  */
object PasswordGenerator {

  val LENGTH = 7
  val SOURCE = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789+@";

  def generatePassword: String = {


    val r = new scala.util.Random
    val idx = (1 to LENGTH).map { _ => r.nextInt(56) }

    val generatedPAss = for {
      index <- idx
      char <- SOURCE.substring(index, index + 1)
    } yield char

    generatedPAss.mkString("")

  }

}


