package gubmint

sealed trait Resolution

object Resolution {
  case object Day extends Resolution
  case object Month extends Resolution
  case object Year extends Resolution

  def fromString(string: String): Resolution = {
    string match {
      case "day" => Day
      case "month" => Month
      case "year" => Year
    }
  }
}
