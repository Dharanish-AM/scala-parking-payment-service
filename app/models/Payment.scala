package models

import java.time.LocalDateTime

case class Payment(
    id: Long,
    entryTime: LocalDateTime,
    exitTime: Option[LocalDateTime],
    durationMinutes: Option[Int],
    calculatedFee: Option[BigDecimal],
    status: PaymentStatus.Value,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
)
