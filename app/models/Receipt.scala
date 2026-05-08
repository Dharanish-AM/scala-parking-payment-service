package models
import java.time.LocalDateTime

case class Receipt(
  id: Long,
  paymentId: Long,
  entryTime: LocalDateTime,
  exitTime: LocalDateTime,
  durationMinutes: Int,
  calculatedFee: BigDecimal,
  createdAt: LocalDateTime
)