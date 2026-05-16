package services

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.concurrent.ScalaFutures
import org.mockito.Mockito._
import models.Receipt
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

  // This part tests how we calculate the parking fee.
  // It checks the time you stayed and makes sure we update the database correctly.
  "PaymentService.calculateFee" should {
    "calculate fee and update payment when exitTime > entryTime" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val entry = LocalDateTime.now().minusMinutes(90)
      val exit = LocalDateTime.now()
      val payment = TestFixtures.payment(id = 1L, entryTime = entry)

      when(repo.findById(1L)).thenReturn(Future.successful(Some(payment)))
      when(
        repo.update(org.mockito.ArgumentMatchers.any(classOf[models.Payment]))
      )
        .thenReturn(Future.successful(1))

      whenReady(service.calculateFee(1L, exit)) { res =>
        res match {
          case Right(CalculateFeeResponse(durationMinutes, fee)) =>
            durationMinutes must be > 0
            fee must be >= BigDecimal(0)
          case Left(err) => fail(s"expected Right, got Left($err)")
        }
      }

      verify(repo).update(
        org.mockito.ArgumentMatchers.any(classOf[models.Payment])
      )
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

    "return not_found when payment does not exist" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val exit = LocalDateTime.now()

      when(repo.findById(30L)).thenReturn(Future.successful(None))

      whenReady(service.calculateFee(30L, exit)) { res =>
        res mustBe Left("not_found")
      }
    }

    "return invalid_exit_time when exitTime == entryTime" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val entry = LocalDateTime.now()
      val exit = entry
      val payment = TestFixtures.payment(id = 31L, entryTime = entry)

      when(repo.findById(31L)).thenReturn(Future.successful(Some(payment)))

      whenReady(service.calculateFee(31L, exit)) { res =>
        res mustBe Left("invalid_exit_time")
      }
    }

    "return update_failed when repo.update returns 0" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val entry = LocalDateTime.now().minusMinutes(60)
      val exit = LocalDateTime.now()
      val payment = TestFixtures.payment(id = 32L, entryTime = entry)

      when(repo.findById(32L)).thenReturn(Future.successful(Some(payment)))
      when(
        repo.update(org.mockito.ArgumentMatchers.any(classOf[models.Payment]))
      ).thenReturn(Future.successful(0))

      whenReady(service.calculateFee(32L, exit)) { res =>
        res mustBe Left("update_failed")
      }
    }

    // You can't recalculate the fee if the payment is already done!
    "return invalid_status when payment is already completed" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val now = LocalDateTime.now()
      val p = TestFixtures.payment(id = 61L, status = PaymentStatus.COMPLETED)

      when(repo.findById(61L)).thenReturn(Future.successful(Some(p)))

      whenReady(service.calculateFee(61L, now)) { res =>
        res mustBe Left("invalid_status")
      }
    }
  }

  // This part tests the final payment step (like clicking "Pay Now").
  "PaymentService.processPayment" should {
    // If they already paid, just tell them it's done instead of charging again.
    "return Right(payment) if already completed (idempotency)" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val now = LocalDateTime.now()
      val payment =
        TestFixtures.payment(id = 3L, status = PaymentStatus.COMPLETED)

      when(repo.findById(3L)).thenReturn(Future.successful(Some(payment)))

      whenReady(service.processPayment(3L)) { res =>
        res mustBe Right(payment)
      }
    }
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
      val payment =
        TestFixtures.payment(id = 4L, entryTime = now.minusMinutes(10))

      when(repo.completeIfFeeCalculated(4L)).thenReturn(Future.successful(0))
      when(repo.findById(4L)).thenReturn(Future.successful(Some(payment)))

      whenReady(service.processPayment(4L)) { res =>
        res mustBe Left("fee_not_calculated")
      }
    }

    "return Left(\"not_found\") when complete updates but payment missing" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)

      when(repo.completeIfFeeCalculated(5L)).thenReturn(Future.successful(1))
      when(repo.findById(5L)).thenReturn(Future.successful(None))

      whenReady(service.processPayment(5L)) { res =>
        res mustBe Left("not_found")
      }
    }

    "return Left(\"update_failed\") when complete returns 0 but fee already calculated" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val now = LocalDateTime.now()
      val payment = TestFixtures.payment(
        id = 6L,
        entryTime = now.minusMinutes(30),
        exitTime = Some(now),
        durationMinutes = Some(30),
        calculatedFee = Some(BigDecimal(10)),
        status = PaymentStatus.PENDING,
        createdAt = now.minusMinutes(30),
        updatedAt = now
      )

      when(repo.completeIfFeeCalculated(6L)).thenReturn(Future.successful(0))
      when(repo.findById(6L)).thenReturn(Future.successful(Some(payment)))

      whenReady(service.processPayment(6L)) { res =>
        res mustBe Left("update_failed")
      }
    }

    "return Left(\"not_found\") when complete returns 0 and payment missing" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)

      when(repo.completeIfFeeCalculated(40L)).thenReturn(Future.successful(0))
      when(repo.findById(40L)).thenReturn(Future.successful(None))

      whenReady(service.processPayment(40L)) { res =>
        res mustBe Left("not_found")
      }
    }
  }

  // This part tests the refund logic.
  "PaymentService.refundPayment" should {
    "return Left(\"not_found\") when payment missing" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)

      when(repo.findById(10L)).thenReturn(Future.successful(None))

      whenReady(service.refundPayment(10L)) { res =>
        res mustBe Left("not_found")
      }
    }

    "return Left(\"not_completed\") when payment is pending" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val p = TestFixtures.payment(id = 11L, status = PaymentStatus.PENDING)

      when(repo.findById(11L)).thenReturn(Future.successful(Some(p)))

      whenReady(service.refundPayment(11L)) { res =>
        res mustBe Left("not_completed")
      }
    }

    "return Left(\"already_refunded\") when payment already refunded" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val p = TestFixtures.payment(id = 12L, status = PaymentStatus.REFUNDED)

      when(repo.findById(12L)).thenReturn(Future.successful(Some(p)))

      whenReady(service.refundPayment(12L)) { res =>
        res mustBe Left("already_refunded")
      }
    }

    "return Right(updatedPayment) when refund succeeds" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val now = LocalDateTime.now()
      val p = TestFixtures.payment(
        id = 13L,
        status = PaymentStatus.COMPLETED,
        exitTime = Some(now),
        durationMinutes = Some(60),
        calculatedFee = Some(BigDecimal(20))
      )
      val refunded = p.copy(status = PaymentStatus.REFUNDED)

      when(repo.findById(13L)).thenReturn(
        Future.successful(Some(p)),
        Future.successful(Some(refunded))
      )
      when(
        repo.update(org.mockito.ArgumentMatchers.any(classOf[models.Payment]))
      ).thenReturn(Future.successful(1))

      whenReady(service.refundPayment(13L)) { res =>
        res match {
          case Right(updated) => updated.status mustBe PaymentStatus.REFUNDED
          case Left(err)      => fail(s"expected Right, got Left($err)")
        }
      }
    }

    "return Left(\"update_failed\") when refund update fails" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val now = LocalDateTime.now()
      val p = TestFixtures.payment(
        id = 14L,
        status = PaymentStatus.COMPLETED,
        exitTime = Some(now),
        durationMinutes = Some(10),
        calculatedFee = Some(BigDecimal(5))
      )

      when(repo.findById(14L)).thenReturn(Future.successful(Some(p)))
      when(
        repo.update(org.mockito.ArgumentMatchers.any(classOf[models.Payment]))
      ).thenReturn(Future.successful(0))

      whenReady(service.refundPayment(14L)) { res =>
        res mustBe Left("update_failed")
      }
    }
  }

  // This part tests generating a receipt that the customer can print.
  "PaymentService.getReceipt" should {
    "return Left(\"not_found\") when payment missing" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)

      when(repo.findById(20L)).thenReturn(Future.successful(None))

      whenReady(service.getReceipt(20L)) { res =>
        res mustBe Left("not_found")
      }
    }

    "return Left(\"receipt_unavailable\") when payment not completed/refunded" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val p = TestFixtures.payment(id = 21L, status = PaymentStatus.PENDING)

      when(repo.findById(21L)).thenReturn(Future.successful(Some(p)))

      whenReady(service.getReceipt(21L)) { res =>
        res mustBe Left("receipt_unavailable")
      }
    }

    "return Left(\"receipt_unavailable\") when fields missing even if completed" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val p = TestFixtures.payment(
        id = 22L,
        status = PaymentStatus.COMPLETED,
        exitTime = None,
        durationMinutes = None,
        calculatedFee = None
      )

      when(repo.findById(22L)).thenReturn(Future.successful(Some(p)))

      whenReady(service.getReceipt(22L)) { res =>
        res mustBe Left("receipt_unavailable")
      }
    }

    "return Right(receipt) when payment completed with all fields" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val now = LocalDateTime.now()
      val p = TestFixtures.payment(
        id = 23L,
        status = PaymentStatus.COMPLETED,
        exitTime = Some(now),
        durationMinutes = Some(45),
        calculatedFee = Some(BigDecimal(20))
      )

      when(repo.findById(23L)).thenReturn(Future.successful(Some(p)))

      whenReady(service.getReceipt(23L)) { res =>
        res match {
          case Right(receipt) =>
            receipt.paymentId mustBe p.id
            receipt.calculatedFee mustBe p.calculatedFee.get
          case Left(err) => fail(s"expected Right, got Left($err)")
        }
      }
    }

    "return Right(receipt) when payment refunded with all fields" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val now = LocalDateTime.now()
      val p = TestFixtures.payment(
        id = 24L,
        status = PaymentStatus.REFUNDED,
        exitTime = Some(now),
        durationMinutes = Some(30),
        calculatedFee = Some(BigDecimal(10))
      )

      when(repo.findById(24L)).thenReturn(Future.successful(Some(p)))

      whenReady(service.getReceipt(24L)) { res =>
        res match {
          case Right(receipt) =>
            receipt.paymentId mustBe p.id
            receipt.calculatedFee mustBe p.calculatedFee.get
          case Left(err) => fail(s"expected Right, got Left($err)")
        }
      }
    }
  }

  // Simple tests for creating and finding payments in the system.
  "PaymentService.create/getPaymentDetails" should {
    "createPayment returns created payment when entryTime is not in future" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val now = LocalDateTime.now()
      val created = TestFixtures.payment(id = 50L, entryTime = now)

      when(repo.create(now)).thenReturn(Future.successful(created))

      whenReady(service.createPayment(now)) { res =>
        res mustBe Right(created)
      }
    }

    "createPayment returns Left(\"future_entry_time\") when entryTime is in future" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val future = LocalDateTime.now().plusDays(1)

      whenReady(service.createPayment(future)) { res =>
        res mustBe Left("future_entry_time")
      }
    }

    "getPaymentDetails returns payment when present" in {
      val repo = mock[PaymentRepository]
      val service = new PaymentService(repo)
      val p = TestFixtures.payment(id = 51L)

      when(repo.findById(51L)).thenReturn(Future.successful(Some(p)))

      whenReady(service.getPaymentDetails(51L)) { res =>
        res mustBe Some(p)
      }
    }
  }
}
