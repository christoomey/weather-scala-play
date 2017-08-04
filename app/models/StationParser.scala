package gubmint

import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, Sink, Source}
import akka.util.ByteString
import scala.concurrent.Future
import java.nio.file.Paths

class StationParser(implicit val mat: Materializer) {
  def find(stationIds: List[String]): Future[List[Station]] =
    inputStream
      .via(parse)
      .runWith(findAllById(stationIds))

  private def inputStream: Source[ByteString, Any] =
    FileIO.fromPath(Paths.get("data/hpd-stations.txt"))

  private def parse: Flow[ByteString, Station, Any] =
    Flow[ByteString]
      .via(Framing.delimiter(ByteString("\n"), Int.MaxValue))
      .map { byteString => byteString.utf8String }
      .map { string => Station(
        id = string.slice(0, 11).trim,
        latitude = BigDecimal(string.slice(12, 20).trim),
        longitude = BigDecimal(string.slice(21, 30).trim),
        elevation = BigDecimal(string.slice(31, 37).trim),
        state = string.slice(38, 40).trim,
        name = string.slice(41, 122).trim,
        wmoId = Option(string.slice(123, 128).trim).filter(_.nonEmpty),
        nominalSamplingInterval = Integer.parseInt(string.slice(129, 133).trim),
        gmtOffset = Integer.parseInt(string.slice(134, 139).trim)
      ) }

  private def findAllById(
      ids: List[String]): Sink[Station, Future[List[Station]]] =
    Flow[Station]
      .filter(station => ids.contains(station.id))
      .toMat(sinkThatMakesAList)(Keep.right)

  private def sinkThatMakesAList =
    Sink.fold[List[Station], Station](List())((list, station) => station :: list)
}
