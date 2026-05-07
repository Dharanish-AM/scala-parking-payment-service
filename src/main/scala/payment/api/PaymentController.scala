package payment.api

import payment.models._
import payment.service.PaymentService

// HTTP endpoints and request handling will be implemented here
trait PaymentController {
  def calculateFee(): Unit
  def createPayment(): Unit
  def getPayment(id: Long): Unit
  def processPayment(id: Long): Unit
  def refundPayment(id: Long): Unit
  def getReceipt(id: Long): Unit
}
