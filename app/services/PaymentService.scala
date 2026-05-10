package services

import javax.inject._
import repositories.PaymentRepository
import scala.concurrent.ExecutionContext
import java.time.LocalDateTime
import scala.concurrent.Future
import models.Payment

@Singleton
class PaymentService @Inject() (paymentRepository: PaymentRepository)(implicit
    ec: ExecutionContext
) {
  def createPayment(entryTime: LocalDateTime): Future[Payment] =
    paymentRepository.create(entryTime)
}
