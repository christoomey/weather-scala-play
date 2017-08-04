package gubmint

// ID            1-11   Character
// LATITUDE     13-20   Real
// LONGITUDE    22-30   Real
// ELEVATION    32-37   Real
// STATE        39-40   Character
// NAME         42-122  Character
// WMO ID       124-128 Character
// NOMINAL SAMPLING INTERVAL 130-133 Character
// N HOURS OFFSET FROM GMT   135-139 Character

case class Station(id: String,
                   latitude: BigDecimal,
                   longitude: BigDecimal,
                   elevation: BigDecimal,
                   state: String,
                   name: String,
                   wmoId: Option[String],
                   nominalSamplingInterval: Int,
                   gmtOffset: Int)
