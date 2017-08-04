package dao

import java.sql.Date
import scala.concurrent.{ ExecutionContext, Future }
import javax.inject.Inject

import models.WeatherQuery
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

class WeatherQueryDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val WeatherQueries = TableQuery[WeatherQueriesTable]

  def all(): Future[Seq[WeatherQuery]] = db.run(WeatherQueries.result)

  def find(id: Int): Future[Option[WeatherQuery]] = {
    db.run(WeatherQueries.filter(_.id === id).result.headOption)
  }

  def insert(weatherQuery: WeatherQuery): Future[WeatherQuery] = {
    db.run((WeatherQueries returning WeatherQueries.map(_.id)) += weatherQuery).map { id =>
      weatherQuery.copy(id = Some(id))
    }
  }

  private class WeatherQueriesTable(tag: Tag) extends Table[WeatherQuery](tag, "weatherQueries") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def resolution = column[String]("resolution")
    def startDate = column[Date]("startDate")
    def endDate = column[Date]("endDate")

    def * = (id.?, resolution, startDate, endDate) <> (WeatherQuery.tupled, WeatherQuery.unapply)
  }
}
