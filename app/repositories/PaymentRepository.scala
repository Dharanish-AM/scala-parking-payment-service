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
}
