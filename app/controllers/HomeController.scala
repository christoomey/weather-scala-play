package controllers

import java.util.Date
import javax.inject._
import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

case class WeatherQuery(
  // stations: List[Int],
  resolution: String,
  startDate: Date,
  endDate: Date,
)

@Singleton
class HomeController @Inject()(cc: ControllerComponents)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  val weatherQueryForm = Form(
    mapping(
      // "stations" -> list(number),
      "resolution" -> text,
      "startDate" -> date,
      "endDate" -> date,
    )(WeatherQuery.apply)(WeatherQuery.unapply)
  )

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index(weatherQueryForm))
  }

  def post() = TODO
}
