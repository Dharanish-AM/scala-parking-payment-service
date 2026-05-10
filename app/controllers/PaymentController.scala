package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}

import dtos.{CreatePaymentRequest, PaymentResponse}
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
  implicit val paymentResponseWrites: OWrites[PaymentResponse] =
    Json.writes[PaymentResponse]

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

  def calculateFee: Action[JsValue] = Action.async(parse.json) { request =>
    Future.successful(Ok(Json.obj("message" -> "calculateFee not yet implemented")))
  }

  def getPaymentDetails(id: Long): Action[AnyContent] = Action.async {
    Future.successful(Ok(Json.obj("message" -> "getPaymentDetails not yet implemented")))
  }

  def processPayment(id: Long): Action[JsValue] = Action.async(parse.json) { request =>
    Future.successful(Ok(Json.obj("message" -> "processPayment not yet implemented")))
  }

  def refundPayment(id: Long): Action[JsValue] = Action.async(parse.json) { request =>
    Future.successful(Ok(Json.obj("message" -> "refundPayment not yet implemented")))
  }

  def getReceipt(id: Long): Action[AnyContent] = Action.async {
    Future.successful(Ok(Json.obj("message" -> "getReceipt not yet implemented")))
  }
}
