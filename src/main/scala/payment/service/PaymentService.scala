package payment.service

import payment.models._
import payment.repository.PaymentRepository

import java.time.LocalDateTime
import scala.concurrent.Future

trait PaymentService {
  // Business logic for payment processing, fee calculation, refunds
  def calculateFee(entryTime: LocalDateTime, exitTime: LocalDateTime): BigDecimal
  def createPayment(payment: Payment): Future[Long]
  def processPayment(paymentId: Long): Future[Boolean]
  def refundPayment(paymentId: Long): Future[Boolean]
}
