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
import models.Receipt
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
  implicit val receiptWrites: OWrites[Receipt] = Json.writes[Receipt]

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
            case Right(payment) =>
              Created(Json.toJson(PaymentMapper.toResponse(payment)))
            case Left("future_entry_time") =>
              BadRequest(
                Json.obj("error" -> "entryTime cannot be in the future")
              )
            case Left(_) =>
              InternalServerError(
                Json.obj("error" -> "Unable to create payment")
              )
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
              case Left("invalid_status") =>
                BadRequest(
                  Json.obj(
                    "error" -> "Fee can only be calculated for PENDING payments"
                  )
                )
              case Left("future_exit_time") =>
                BadRequest(
                  Json.obj("error" -> "exitTime cannot be in the future")
                )
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
    paymentService.getPaymentDetails(id).map {
      case Some(payment) => Ok(Json.toJson(PaymentMapper.toResponse(payment)))
      case None          =>
        NotFound(Json.obj("error" -> s"Payment with id $id not found"))
    }
  }

  def refundPayment(id: Long): Action[JsValue] = Action.async(parse.json) { _ =>
    paymentService.refundPayment(id).map {
      case Right(payment) => Ok(Json.toJson(PaymentMapper.toResponse(payment)))
      case Left("not_found") =>
        NotFound(Json.obj("error" -> s"Payment with id $id not found"))
      case Left("not_completed") =>
        BadRequest(
          Json.obj("error" -> "Only completed payments can be refunded")
        )
      case Left("already_refunded") =>
        BadRequest(Json.obj("error" -> "Payment has already been refunded"))
      case Left(_) =>
        InternalServerError(Json.obj("error" -> "Unable to refund payment"))
    }
  }

  def getReceipt(id: Long): Action[AnyContent] = Action.async {
    paymentService.getReceipt(id).map {
      case Right(receipt)    => Ok(Json.toJson(receipt))
      case Left("not_found") =>
        NotFound(Json.obj("error" -> s"Payment with id $id not found"))
      case Left("receipt_unavailable") =>
        BadRequest(
          Json.obj(
            "error" -> "Receipt is available only after the payment is completed"
          )
        )
      case Left(_) =>
        InternalServerError(Json.obj("error" -> "Unable to load receipt"))
    }
  }
}
