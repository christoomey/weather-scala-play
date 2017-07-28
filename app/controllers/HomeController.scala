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

  def validate(weatherQuery : WeatherQuery) = {
    weatherQuery.resolution match {
      case "Week" =>
        Some(weatherQuery)
      case _ =>
        None
    }
  }

  val weatherQueryForm = Form(
    mapping(
      // "stations" -> list(number),
      "resolution" -> text,
      "startDate" -> date,
      "endDate" -> date,
      )(WeatherQuery.apply)(WeatherQuery.unapply) verifying("Badddd stuff", fields => fields match {
        case weatherQuery => validate(weatherQuery).isDefined
      })
  )

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index(weatherQueryForm))
  }

  def post() = Action { implicit request =>
    val weatherQuery = request.body
    println(weatherQuery)

    weatherQueryForm.bindFromRequest.fold(
      formWithErrors => {
        Ok("you made a bad from!!!!!")
      },
      weatherQuery => {
        Ok("very GOOD FROM!")
      }
    )
  }
}
