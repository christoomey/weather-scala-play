package gubmint

import akka.Done
import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, Sink, Source}
import akka.util.ByteString
import scala.concurrent.Future
import java.nio.file.Paths

class DataParser(
  resolution: Resolution,
  startDate: (Int, Int, Int),
  endDate: (Int, Int, Int))(
    implicit val mat: Materializer) {
  import Resolution.{Year, Month, Day}

  def find(stations: List[Station]): Future[List[Result]] =
    Source(stations)
      .via(superFlow)
      .toMat(sinkThatMakesAList)(Keep.right)
      .run()

  def weatherDataToResult: Flow[(Station, WeatherData), Result, Any] =
    Flow[(Station, WeatherData)]
      .mapConcat { case (station, weatherData) =>
        weatherData.dataPackets.collect {
          case dataPacket if dataPacket.value != -9999 =>
            Result(station = station,
              key = weatherData.day,
              year = weatherData.year,
              month = weatherData.month,
              day = weatherData.day,
              rainfall = dataPacket.value)
        }
      }

  def superFlow: Flow[Station, Result, Any] =
    Flow[Station]
      .mapAsync(4)(dataFor(_))
      .mapConcat(identity)

  def resolve(resolution: Resolution): Flow[Result, Result, Any] =
    resolution match {
      case Year => Flow[Result].via(aggregateResult(Year)).via(resolve(Month))
      case Month => Flow[Result].via(aggregateResult(Month)).via(resolve(Day))
      case Day => Flow[Result].via(aggregateResult(Day))
    }

  def aggregateResult(resolution: Resolution): Flow[Result, Result, Any] = {
    val nextKey: Result => Int = resolution match {
      case Day => (_.month)
      case Month => (_.year)
      case Year => (_ => 0)
    }
    val key: Result => Int = resolution match {
      case Day => (x => x.day + (x.month * 100) + (x.year * 10000))
      case Month => (x => x.month + x.year + 100)
      case Year => (_.year)
    }
    Flow[Result]
      .statefulMapConcat { () =>
        var state: Option[Result] = None

        result => {
          val currentResult = state.getOrElse(result)

          if (key(result) == key(currentResult)) {
            state = Some(Result(station = currentResult.station,
                  key = key(currentResult),
                  year = currentResult.year,
                  month = currentResult.month,
                  day = currentResult.day,
                  rainfall = currentResult.rainfall + result.rainfall))
            List()
          } else {
            val thingToReturn = currentResult.copy(key = nextKey(currentResult))
            state = Some(result)
            List(thingToReturn)
          }
        }
      }
  }

  def dataFor(station: Station): Future[List[Result]] =
    inputStream(station.id)
      .via(parse)
      .filter(dateBetween(startDate, endDate))
      .map((station, _))
      .via(weatherDataToResult)
      .via(resolve(resolution))
      .toMat(sinkThatMakesAList)(Keep.right)
      .run()

  private def dateBetween(startDate: (Int, Int, Int), endDate: (Int, Int, Int))(weatherData: WeatherData) = {
    isAfter(weatherData, startDate) && isBefore(weatherData, endDate)
  }

  private def isAfter(weatherData: WeatherData, startDate: (Int, Int, Int)): Boolean = {
    startDate match {
      case (year, month, day) =>
        weatherData.year > year ||
        weatherData.year == year &&
          (weatherData.month > month ||
            (weatherData.month == month && weatherData.day > day))
    }
  }

  private def isBefore(weatherData: WeatherData, endDate: (Int, Int, Int)): Boolean = {
    endDate match {
      case (year, month, day) =>
        weatherData.year < year ||
        weatherData.year == year &&
          (weatherData.month < month ||
            (weatherData.month == month && weatherData.day < day))
    }
  }

  private def inputStream(stationId: String): Source[ByteString, Any] =
    FileIO.fromPath(Paths.get(s"data/all/${stationId}.hly"))

  private def parse: Flow[ByteString, WeatherData, Any] =
    Flow[ByteString]
      .via(Framing.delimiter(ByteString("\n"), Int.MaxValue))
      .map { byteString => byteString.utf8String }
      .map { string => WeatherData(
        id = string.slice(0, 11).trim,
        year = Integer.parseInt(string.slice(11, 15).trim),
        month = Integer.parseInt(string.slice(15, 17).trim),
        day = Integer.parseInt(string.slice(17, 19).trim),
        element = string.slice(19, 23).trim,
        dataPackets = (0 until 24).toList.map { n =>
          val dataPatcketString = string.slice((n * 9) + 23, (n * 9) + 32)
          DataPacket(
            hourOfDay = n + 1,
            value = Integer.parseInt(dataPatcketString.slice(0,5).trim),
            mFlag = dataPatcketString.charAt(5),
            qFlag = dataPatcketString.charAt(6),
            sFlag = dataPatcketString.charAt(7),
            s2Flag = dataPatcketString.charAt(8)
          )
        }
      )}

  private def sinkThatMakesAList[A] =
    Sink.fold[List[A], A](List())((list, element) => element :: list)
}
