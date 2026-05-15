package repositories

import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDateTime

import models.{Payment, PaymentStatus}
import tables.PaymentTable

@Singleton
class PaymentRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  implicit val paymentStatusColumnType: BaseColumnType[PaymentStatus.Value] =
    MappedColumnType.base[PaymentStatus.Value, String](
      _.toString,
      PaymentStatus.withName
    )

  private val payments = TableQuery[PaymentTable]
  def create(entryTime: LocalDateTime): Future[Payment] = {
    val now = LocalDateTime.now()

    val newPayment = Payment(
      id = 0L,
      entryTime = entryTime,
      exitTime = None,
      durationMinutes = None,
      calculatedFee = None,
      status = PaymentStatus.PENDING,
      createdAt = now,
      updatedAt = now
    )

    val insertQuery =
      payments returning payments.map(_.id) into { (payment, generatedId) =>
        payment.copy(id = generatedId)
      }

    db.run(insertQuery += newPayment)
  }

  def findById(id: Long): Future[Option[Payment]] = {
    db.run(
      payments
        .filter(_.id === id)
        .result
        .headOption
    )
  }

  def update(payment: Payment): Future[Int] = {
    db.run(
      payments
        .filter(_.id === payment.id)
        .update(payment.copy(updatedAt = LocalDateTime.now()))
    )
  }

  def completeIfFeeCalculated(id: Long): Future[Int] = {
    val now = LocalDateTime.now()
    db.run(
      payments
        .filter(p =>
          p.id === id && p.calculatedFee.isDefined && p.status === PaymentStatus.PENDING
        )
        .map(p => (p.status, p.updatedAt))
        .update((PaymentStatus.COMPLETED, now))
    )
  }

}
