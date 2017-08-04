package gubmint

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import scala.concurrent.{ExecutionContext, Future}

object Main {
  implicit val system = ActorSystem("gubmint")
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  def theThingWeDo(stationIds: List[String], startDate: (Int, Int, Int), endDate: (Int, Int, Int), resolution: Resolution): Future[List[gubmint.WeatherResult]] = {
    val stationParser = new StationParser
    val dataParser = new DataParser(resolution, startDate, endDate)
    for {
      stations <- Future.unit.flatMap(_ => stationParser.find(stationIds))
      results <- Future.unit.flatMap(_ => dataParser.find(stations))
    } yield {
      results
    }
  }
}
