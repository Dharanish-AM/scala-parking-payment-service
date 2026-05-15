package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}

import dtos.{
  CalculateFeeRequest,
  CalculateFeeResponse,
  CreatePaymentRequest,
  PaymentResponse
}
import mappers.PaymentMapper
import services.PaymentService
import dtos.DateTimeFormats._
import models.PaymentStatus._

@Singleton
class PaymentController @Inject() (
    cc: ControllerComponents,
    paymentService: PaymentService
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  implicit val createPaymentRequestReads: Reads[CreatePaymentRequest] =
    Json.reads[CreatePaymentRequest]
  implicit val calculateFeeRequestReads: Reads[CalculateFeeRequest] =
    Json.reads[CalculateFeeRequest]
  implicit val paymentResponseWrites: OWrites[PaymentResponse] =
    Json.writes[PaymentResponse]
  implicit val calculateFeeResponseWrites: OWrites[CalculateFeeResponse] =
    Json.writes[CalculateFeeResponse]

  def createPayment: Action[JsValue] = Action.async(parse.json) { request =>
    request.body
      .validate[CreatePaymentRequest]
      .fold(
        errors =>
          Future.successful(
            BadRequest(Json.obj("errors" -> JsError.toJson(errors)))
          ),
        createPaymentRequest =>
          paymentService.createPayment(createPaymentRequest.entryTime).map {
            payment =>
              Created(Json.toJson(PaymentMapper.toResponse(payment)))
          }
      )
  }

  def calculateFee(id: Long): Action[JsValue] = Action.async(parse.json) {
    request =>
      request.body
        .validate[CalculateFeeRequest]
        .fold(
          errors =>
            Future.successful(
              BadRequest(Json.obj("errors" -> JsError.toJson(errors)))
            ),
          calculateFeeRequest =>
            paymentService.calculateFee(id, calculateFeeRequest.exitTime).map {
              case Right(response) =>
                Ok(Json.toJson(response))
              case Left("not_found") =>
                NotFound(Json.obj("error" -> s"Payment with id $id not found"))
              case Left("invalid_exit_time") =>
                BadRequest(
                  Json.obj("error" -> "exitTime must be after entryTime")
                )
              case Left(_) =>
                InternalServerError(
                  Json.obj("error" -> "Unable to calculate fee")
                )
            }
        )
  }

  def processPayment(id: Long): Action[AnyContent] = Action.async { request =>
    paymentService.processPayment(id).map {
      case Right(payment) => Ok(Json.toJson(PaymentMapper.toResponse(payment)))
      case Left("not_found") =>
        NotFound(Json.obj("error" -> s"Payment with id $id not found"))
      case Left("fee_not_calculated") =>
        BadRequest(
          Json
            .obj("error" -> "Fee must be calculated before processing payment")
        )
      case Left("update_failed") =>
        InternalServerError(
          Json.obj("error" -> "Unable to update payment status")
        )
      case Left(_) =>
        InternalServerError(Json.obj("error" -> "Unable to process payment"))
    }
  }

  def getPaymentDetails(id: Long): Action[AnyContent] = Action.async {
    Future.successful(
      Ok(Json.obj("message" -> "getPaymentDetails not yet implemented"))
    )
  }

  def refundPayment(id: Long): Action[JsValue] = Action.async(parse.json) {
    request =>
      Future.successful(
        Ok(Json.obj("message" -> "refundPayment not yet implemented"))
      )
  }

  def getReceipt(id: Long): Action[AnyContent] = Action.async {
    Future.successful(
      Ok(Json.obj("message" -> "getReceipt not yet implemented"))
    )
  }
}
