package mappers

import dtos.PaymentResponse
import models.Payment

object PaymentMapper {
  def toResponse(payment: Payment): PaymentResponse =
    PaymentResponse(
      id = payment.id,
      entryTime = payment.entryTime,
      exitTime = payment.exitTime,
      durationMinutes = payment.durationMinutes,
      calculatedFee = payment.calculatedFee,
      status = payment.status,
      createdAt = payment.createdAt,
      updatedAt = payment.updatedAt
    )
}
