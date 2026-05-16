package repositories

import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDateTime
import java.sql.Timestamp

import models.{Payment, PaymentStatus}
import tables.PaymentTable

import slick.jdbc.MySQLProfile

@Singleton
class PaymentRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[MySQLProfile] {

  import profile.api._

  implicit val paymentStatusColumnType: BaseColumnType[PaymentStatus.Value] =
    MappedColumnType.base[PaymentStatus.Value, String](
      _.toString,
      PaymentStatus.withName
    )

  implicit val localDateTimeColumnType: BaseColumnType[LocalDateTime] =
    MappedColumnType.base[LocalDateTime, Timestamp](
      Timestamp.valueOf,
      _.toLocalDateTime
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
    findById(id).flatMap {
      case Some(payment)
          if payment.calculatedFee.isDefined && payment.status == PaymentStatus.PENDING =>
        update(
          payment
            .copy(status = PaymentStatus.COMPLETED, updatedAt = LocalDateTime.now())
        )
      case _ => Future.successful(0)
    }
  }

  def ping(): Future[Unit] = {
    db.run(sql"SELECT 1".as[Int]).map(_ => ())
  }
}
