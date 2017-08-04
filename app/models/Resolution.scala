package gubmint

sealed trait Resolution

object Resolution {
  case object Day extends Resolution
  case object Month extends Resolution
  case object Year extends Resolution
}
