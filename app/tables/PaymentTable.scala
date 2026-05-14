package tables

import slick.jdbc.MySQLProfile.api._
import java.time.LocalDateTime
import java.sql.Timestamp
import models.{Payment, PaymentStatus}

class PaymentTable(tag: Tag) extends Table[Payment](tag, "payments") {
  implicit val localDateTimeColumnType: BaseColumnType[LocalDateTime] =
    MappedColumnType.base[LocalDateTime, Timestamp](
      Timestamp.valueOf,
      _.toLocalDateTime
    )

  implicit val paymentStatusColumnType: BaseColumnType[PaymentStatus.Value] =
    MappedColumnType.base[PaymentStatus.Value, String](
      _.toString,
      PaymentStatus.withName
    )

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def entryTime = column[LocalDateTime]("entry_time")
  def exitTime = column[Option[LocalDateTime]]("exit_time")
  def durationMinutes = column[Option[Int]]("duration_minutes")
  def calculatedFee = column[Option[BigDecimal]]("calculated_fee")
  def status = column[PaymentStatus.Value]("status")
  def createdAt = column[LocalDateTime]("created_at")
  def updatedAt = column[LocalDateTime]("updated_at")

  def * = (
    id,
    entryTime,
    exitTime,
    durationMinutes,
    calculatedFee,
    status,
    createdAt,
    updatedAt
  ) <> ((Payment.apply _).tupled, Payment.unapply)

}

object PaymentTable {
  val table = TableQuery[PaymentTable]
}
