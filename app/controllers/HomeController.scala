package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.ExecutionContext

@Singleton
class HomeController @Inject() (cc: ControllerComponents)(implicit
    ec: ExecutionContext
) extends AbstractController(cc) {

  def index: Action[AnyContent] = Action {
    Ok(views.html.home()).as(HTML)
  }
}
