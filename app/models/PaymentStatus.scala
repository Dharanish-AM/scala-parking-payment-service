package models

import play.api.libs.json._

object PaymentStatus extends Enumeration {
  val PENDING = Value("PENDING")
  val COMPLETED = Value("COMPLETED")
  val REFUNDED = Value("REFUNDED")

  type PaymentStatus = Value

  implicit val paymentStatusReads: Reads[PaymentStatus.Value] = Reads { json =>
    json.validate[String].flatMap { s =>
      try JsSuccess(PaymentStatus.withName(s)) catch {
        case _: NoSuchElementException => JsError("Invalid PaymentStatus")
      }
    }
  }
  implicit val paymentStatusWrites: Writes[PaymentStatus.Value] = Writes(s => JsString(s.toString))
  implicit val paymentStatusFormat: Format[PaymentStatus.Value] = Format(paymentStatusReads, paymentStatusWrites)
}