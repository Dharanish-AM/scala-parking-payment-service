package services

import javax.inject._
import repositories.PaymentRepository
import scala.concurrent.ExecutionContext
import java.time.LocalDateTime
import scala.concurrent.Future
import models.Payment
import dtos.CalculateFeeResponse
import utils.calculateParkingFee

@Singleton
class PaymentService @Inject() (paymentRepository: PaymentRepository)(implicit
    ec: ExecutionContext
) {
  def createPayment(entryTime: LocalDateTime): Future[Payment] =
    paymentRepository.create(entryTime)

  def calculateFee(
      id: Long,
      exitTime: LocalDateTime
  ): Future[Either[String, CalculateFeeResponse]] = {
    paymentRepository.findById(id).flatMap {
      case Some(payment) =>
        if (!exitTime.isAfter(payment.entryTime)) {
          Future.successful(Left("invalid_exit_time"))
        } else {
          val durationMinutes = java.time.Duration
            .between(payment.entryTime, exitTime)
            .toMinutes
          .toInt

          val fee = calculateParkingFee.calculateParkingFee(durationMinutes)
          val updatedPayment = payment.copy(
            exitTime = Some(exitTime),
            durationMinutes = Some(durationMinutes),
            calculatedFee = Some(fee)
          )

          paymentRepository.update(updatedPayment).map { _ =>
            Right(
              CalculateFeeResponse(durationMinutes = durationMinutes, fee = fee)
            )
          }
        }
      case None => Future.successful(Left("not_found"))
    }
  }
}
