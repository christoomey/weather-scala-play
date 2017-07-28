package models

import java.sql.Date

case class WeatherQuery(
  id: Option[Int],
  resolution: String,
  startDate: Date,
  endDate: Date,
)
