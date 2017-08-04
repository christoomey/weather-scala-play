package gubmint

// ------------------------------
// Variable   Columns   Type
// ------------------------------
// ID            1-11   Character
// YEAR         12-15   Integer
// MONTH        16-17   Integer
// DAY          18-19   Integer
// ELEMENT      20-23   Character
// VALUE1       24-28   Integer
// MFLAG1       29-29   Character
// QFLAG1       30-30   Character
// SFLAG1       31-31   Character
// S2FLAG1      32-32   Character
// VALUE2       33-37   Integer
// MFLAG2       38-38   Character
// QFLAG2       39-39   Character
// SFLAG2       40-40   Character
// S2FLAG2      41-41   Character
//   .           .          .
//   .           .          .
//   .           .          .
// VALUE24    231-235   Integer
// MFLAG24    236-236   Character
// QFLAG24    237-237   Character
// SFLAG24    238-238   Character
// S2FLAG24   239-239   Character
// ------------------------------

case class WeatherData(id: String,
                       year: Int,
                       month: Int,
                       day: Int,
                       element: String,
                       dataPackets: List[DataPacket])

case class DataPacket(hourOfDay: Int,
                      value: Int,
                      mFlag: Char,
                      qFlag: Char,
                      sFlag: Char,
                      s2Flag: Char)
