package gubmint

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import scala.concurrent.{ExecutionContext, Future}

object Main {
  implicit val system = ActorSystem("gubmint")
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  def main(args: Array[String]): Unit = {
    theThingWeDo(stationIds = List("CQC00914080", "RQC00661901"),
                 startDate = (1999, 5, 12),
                 endDate = (2016, 6, 12),
                 resolution = Resolution.Year)
  }

  def theThingWeDo(stationIds: List[String], startDate: (Int, Int, Int), endDate: (Int, Int, Int), resolution: Resolution): Unit = {
    val future = {
      val stationParser = new StationParser
      val dataParser = new DataParser(resolution, startDate, endDate)
      for {
        stations <- Future.unit.flatMap(_ => stationParser.find(stationIds))
        results <- Future.unit.flatMap(_ => dataParser.find(stations))
      } yield {
        println(results)
        results
      }
    }

    future.onComplete { _ =>
      system.terminate
    }

    future.failed.foreach { exception =>
      throw(exception)
    }
  }
}
