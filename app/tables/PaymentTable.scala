package tables

import slick.jdbc.MySQLProfile.api._
import java.time.LocalDateTime
import models.{Payment, PaymentStatus}

class PaymentTable(tag: Tag) extends Table[Payment](tag, "payments") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def entryTime = column[LocalDateTime]("entry_time")
  def exitTime = column[Option[LocalDateTime]]("exit_time")
  def durationMinutes = column[Option[Int]]("duration_minutes")
  def calculatedFee = column[Option[BigDecimal]]("calculated_fee")
  def status = column[String]("status")
  def createdAt = column[LocalDateTime]("created_at")
  def updatedAt = column[LocalDateTime]("updated_at")
}
