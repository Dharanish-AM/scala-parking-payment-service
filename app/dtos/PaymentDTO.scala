package dtos

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import play.api.libs.json._
import models.PaymentStatus

case class CreatePaymentRequest(
    entryTime: LocalDateTime
)

case class PaymentResponse(
    id: Long,
    entryTime: LocalDateTime,
    exitTime: Option[LocalDateTime],
    durationMinutes: Option[Int],
    calculatedFee: Option[BigDecimal],
    status: PaymentStatus.PaymentStatus,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
)

case class CalculateFeeRequest(
    exitTime: LocalDateTime
)

case class CalculateFeeResponse(
    durationMinutes: Int,
    fee: BigDecimal
)

object DateTimeFormats {
  private val dtf: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  implicit val localDateTimeReads: Reads[LocalDateTime] =
    Reads { json =>
      json.validate[String].flatMap { s =>
        scala.util.Try(LocalDateTime.parse(s, dtf)) match {
          case scala.util.Success(dateTime) => JsSuccess(dateTime)
          case scala.util.Failure(_)        =>
            JsError("error.expected.datetime.iso_local_date_time")
        }
      }
    }

  implicit val localDateTimeWrites: Writes[LocalDateTime] =
    Writes(dt => JsString(dtf.format(dt)))

  implicit val localDateTimeFormat: Format[LocalDateTime] =
    Format(localDateTimeReads, localDateTimeWrites)
}
