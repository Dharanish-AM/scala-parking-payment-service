package dtos

import java.time.LocalDateTime
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
  entryTime: LocalDateTime,
  exitTime: LocalDateTime
)

case class CalculateFeeResponse(
  durationMinutes: Int,
  fee: BigDecimal,
  breakdown: String 
)