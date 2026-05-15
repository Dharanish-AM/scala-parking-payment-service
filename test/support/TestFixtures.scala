package test.support

import java.time.LocalDateTime
import models.{Payment, PaymentStatus}

object TestFixtures {
  def now(): LocalDateTime = LocalDateTime.now()

  def payment(
      id: Long = 1L,
      entryTime: LocalDateTime = now().minusMinutes(60),
      exitTime: Option[LocalDateTime] = None,
      durationMinutes: Option[Int] = None,
      calculatedFee: Option[BigDecimal] = None,
      status: PaymentStatus.Value = PaymentStatus.PENDING,
      createdAt: LocalDateTime = now().minusMinutes(60),
      updatedAt: LocalDateTime = now().minusMinutes(60)
  ): Payment =
    Payment(id, entryTime, exitTime, durationMinutes, calculatedFee, status, createdAt, updatedAt)
}