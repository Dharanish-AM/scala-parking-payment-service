package services

import javax.inject._
import repositories.PaymentRepository
import scala.concurrent.ExecutionContext
import java.time.LocalDateTime
import scala.concurrent.Future
import models.{Payment, PaymentStatus, Receipt}
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

  def processPayment(id: Long): Future[Either[String, Payment]] = {

    paymentRepository.completeIfFeeCalculated(id).flatMap {
      case 1 =>

        paymentRepository.findById(id).map {
          case Some(p) => Right(p)
          case None    => Left("not_found")
        }
      case 0 =>

        paymentRepository.findById(id).map {
          case None          => Left("not_found")
          case Some(payment) =>
            payment.calculatedFee match {
              case None    => Left("fee_not_calculated")
              case Some(_) => Left("update_failed")
            }
        }
    }
  }

  def getPaymentDetails(id: Long): Future[Option[Payment]] =
    paymentRepository.findById(id)

  def refundPayment(id: Long): Future[Either[String, Payment]] = {
    paymentRepository.findById(id).flatMap {
      case None => Future.successful(Left("not_found"))
      case Some(payment) =>
        payment.status match {
          case PaymentStatus.PENDING =>
            Future.successful(Left("not_completed"))
          case PaymentStatus.REFUNDED =>
            Future.successful(Left("already_refunded"))
          case PaymentStatus.COMPLETED =>
            paymentRepository
              .update(payment.copy(status = PaymentStatus.REFUNDED))
              .flatMap {
                case 1 =>
                  paymentRepository.findById(id).map {
                    case Some(updatedPayment) => Right(updatedPayment)
                    case None                 => Left("not_found")
                  }
                case _ => Future.successful(Left("update_failed"))
              }
        }
    }
  }

  def getReceipt(id: Long): Future[Either[String, Receipt]] = {
    paymentRepository.findById(id).map {
      case None => Left("not_found")
      case Some(payment)
          if payment.status == PaymentStatus.COMPLETED || payment.status == PaymentStatus.REFUNDED =>
        (payment.exitTime, payment.durationMinutes, payment.calculatedFee) match {
          case (Some(exitTime), Some(durationMinutes), Some(calculatedFee)) =>
            Right(
              Receipt(
                id = payment.id,
                paymentId = payment.id,
                entryTime = payment.entryTime,
                exitTime = exitTime,
                durationMinutes = durationMinutes,
                calculatedFee = calculatedFee,
                createdAt = payment.createdAt
              )
            )
          case _ => Left("receipt_unavailable")
        }
      case Some(_) => Left("receipt_unavailable")
    }
  }

}
