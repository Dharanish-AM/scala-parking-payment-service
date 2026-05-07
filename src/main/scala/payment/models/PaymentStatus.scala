package payment.models

object PaymentStatus extends Enumeration {
  val PENDING = Value("PENDING")         // Car is parked, waiting to exit
  val COMPLETED = Value("COMPLETED")     // Fee calculated & payment received
  val REFUNDED = Value("REFUNDED")       // Payment refunded

  type PaymentStatus = Value
}
