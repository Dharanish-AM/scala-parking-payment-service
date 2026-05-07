package payment.repository

import payment.models._
import slick.jdbc.MySQLProfile.api._

import java.time.LocalDateTime
import scala.concurrent.Future

trait PaymentRepository {
  // Database query definitions and CRUD operations will be implemented here
  def create(payment: Payment): Future[Long]
  def findById(id: Long): Future[Option[Payment]]
  def update(payment: Payment): Future[Int]
  def findAll(): Future[List[Payment]]
}
