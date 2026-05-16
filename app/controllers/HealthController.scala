package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}

import repositories.PaymentRepository

@Singleton
class HealthController @Inject() (
    cc: ControllerComponents,
    paymentRepository: PaymentRepository
)(implicit
    ec: ExecutionContext
) extends AbstractController(cc) {
  def health: Action[AnyContent] = Action.async {
    paymentRepository.ping().map { _ =>
      Ok(
        Json.obj(
          "status" -> "OK",
          "database" -> "UP",
          "message" -> "Parking Payment Service is healthy"
        )
      )
    }.recover {
      case e: Exception =>
        InternalServerError(
          Json.obj(
            "status" -> "ERROR",
            "database" -> "DOWN",
            "message" -> e.getMessage
          )
        )
    }
  }
}
