package controllers

import java.sql.Date
import scala.concurrent.{ ExecutionContext, Future, Await }
import scala.concurrent.duration._
import javax.inject._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import dao.WeatherQueryDAO
import gubmint.Main.{ theThingWeDo => getWeatherResults }
import gubmint.Resolution
import models.WeatherQuery

@Singleton
class WeatherQueriesController @Inject()(
  cc: ControllerComponents,
  weatherQueryDAO: WeatherQueryDAO,
)(implicit executionContext: ExecutionContext) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  def validateDateRange(startDate: Date, endDate: Date) = {
    startDate.before(endDate)
  }

  val weatherQueryForm = Form(
    mapping(

      "id" -> optional(number),
      "resolution" -> nonEmptyText,
      "startDate" -> sqlDate,
      "endDate" -> sqlDate,
    )(WeatherQuery.apply)(WeatherQuery.unapply) verifying(
      "startDate must come before endDate",
      fields => fields match {
        case weatherQuery => validateDateRange(weatherQuery.startDate, weatherQuery.endDate)
      }
    )
  )

  def index() = Action.async { implicit request: Request[AnyContent] =>
    weatherQueryDAO.all().map { weatherQueries =>
      Ok(views.html.weather_queries.index(weatherQueryForm, weatherQueries))
    }
  }

  def show(id: Int) = Action.async { implicit request =>
    weatherQueryDAO.find(id).map { weatherQueryOption =>
      weatherQueryOption match {
        case Some(weatherQuery) => {
          val resolvedWeather = getWeatherResults(
            stationIds = List("CQC00914080", "RQC00661901"),
            startDate = throopleFromDate(weatherQuery.startDate),
            endDate = throopleFromDate(weatherQuery.endDate),
            resolution = Resolution.fromString(weatherQuery.resolution)
          )

          val results = Await.result(resolvedWeather, 10.seconds)

          Ok(views.html.weather_queries.show(results, weatherQuery))
        }
        case None => { NotFound(s"$id iz Bad From!") }
      }
    }
  }

  def throopleFromDate(date: Date): (Int, Int, Int) = {
    (date.getYear() + 1900, date.getMonth() + 1, date.getDate())
  }

  def post() = Action.async { implicit request =>
    weatherQueryForm.bindFromRequest.fold(
      formWithErrors => {
        weatherQueryDAO.all().map { weatherQueries =>
          Ok(views.html.weather_queries.index(formWithErrors, weatherQueries)) }
      },
      weatherQuery => {
        weatherQueryDAO.insert(weatherQuery).map(weatherQuery => {
          weatherQuery.id match {
            case Some(id) => Redirect(routes.WeatherQueriesController.show(id))
            case None => Ok("very very confusingly bad froom")
          }
        })
      }
    )
  }
}
