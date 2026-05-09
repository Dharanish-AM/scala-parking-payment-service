package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HealthController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc){
    def health: Action[AnyContent] = Action{
        Ok(Json.obj("status" -> "OK", "message" -> "Parking Payment Service is healthy"))
    }
}