package payment.models

import java.time.LocalDateTime

case class Receipt(
  id: Long,
  paymentId: Long,
  totalAmount: BigDecimal,
  issuedAt: LocalDateTime
)
