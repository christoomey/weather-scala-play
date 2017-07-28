package controllers

import java.util.Date
import javax.inject._
import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

case class WeatherQuery(
  resolution: String,
  startDate: Date,
  endDate: Date,
)

@Singleton
class WeatherQueriesController @Inject()(cc: ControllerComponents)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  def validateDateRange(startDate: Date, endDate: Date) = {
    startDate.before(endDate)
  }

  val weatherQueryForm = Form(
    mapping(
      "resolution" -> nonEmptyText,
      "startDate" -> date,
      "endDate" -> date,
      )(WeatherQuery.apply)(WeatherQuery.unapply) verifying(
        "startDate must come before endDate",
        fields => fields match {
          case weatherQuery => validateDateRange(weatherQuery.startDate, weatherQuery.endDate)
        }
      )
  )

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.weather_queries.index(weatherQueryForm))
  }

  def post() = Action { implicit request =>
    weatherQueryForm.bindFromRequest.fold(
      formWithErrors => {
        Ok(views.html.weather_queries.index(formWithErrors))
      },
      weatherQuery => {
        Ok("very GOOD FROM!")
      }
    )
  }
}
