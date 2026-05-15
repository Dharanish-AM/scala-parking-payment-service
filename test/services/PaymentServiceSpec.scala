package services

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.concurrent.ScalaFutures
import org.mockito.Mockito._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import repositories.PaymentRepository
import models.PaymentStatus
import java.time.LocalDateTime
import dtos.CalculateFeeResponse
import test.support.TestFixtures
import org.mockito.MockitoSugar

class PaymentServiceSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with MockitoSugar {

  "PaymentService.calculateFee" should {
    "calculate fee and update payment when exitTime > entryTime" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val entry = LocalDateTime.now().minusMinutes(90)
      val exit = LocalDateTime.now()
      val payment = TestFixtures.payment(id = 1L, entryTime = entry)

      when(repo.findById(1L)).thenReturn(Future.successful(Some(payment)))
      when(repo.update(org.mockito.ArgumentMatchers.any(classOf[models.Payment])))
        .thenReturn(Future.successful(1))

      whenReady(service.calculateFee(1L, exit)) { res =>
        res match {
          case Right(CalculateFeeResponse(durationMinutes, fee)) =>
            durationMinutes must be > 0
            fee must be >= BigDecimal(0)
          case Left(err) => fail(s"expected Right, got Left($err)")
        }
      }

      verify(repo).update(org.mockito.ArgumentMatchers.any(classOf[models.Payment]))
    }

    "return invalid_exit_time when exitTime <= entryTime" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val entry = LocalDateTime.now()
      val exit = entry.minusMinutes(1)
      val payment = TestFixtures.payment(id = 2L, entryTime = entry)

      when(repo.findById(2L)).thenReturn(Future.successful(Some(payment)))

      whenReady(service.calculateFee(2L, exit)) { res =>
        res mustBe Left("invalid_exit_time")
      }
    }
  }

  "PaymentService.processPayment" should {
    "return Right(payment) when completeIfFeeCalculated updates a row" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val now = LocalDateTime.now()
      val payment = TestFixtures.payment(
        id = 3L,
        entryTime = now.minusMinutes(60),
        exitTime = Some(now),
        durationMinutes = Some(60),
        calculatedFee = Some(BigDecimal(5)),
        status = PaymentStatus.COMPLETED,
        createdAt = now.minusMinutes(60),
        updatedAt = now
      )

      when(repo.completeIfFeeCalculated(3L)).thenReturn(Future.successful(1))
      when(repo.findById(3L)).thenReturn(Future.successful(Some(payment)))

      whenReady(service.processPayment(3L)) { res =>
        res mustBe Right(payment)
      }
    }

    "return Left(\"fee_not_calculated\") when fee not calculated" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val now = LocalDateTime.now()
      val payment = TestFixtures.payment(id = 4L, entryTime = now.minusMinutes(10))

      when(repo.completeIfFeeCalculated(4L)).thenReturn(Future.successful(0))
      when(repo.findById(4L)).thenReturn(Future.successful(Some(payment)))

      whenReady(service.processPayment(4L)) { res =>
        res mustBe Left("fee_not_calculated")
      }
    }
  }
}