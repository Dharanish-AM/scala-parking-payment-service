package controllers

import org.scalatestplus.play.PlaySpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._
import java.time.LocalDateTime

class FullFlowSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures {

  "Full payment flow" should {
    "calculate -> process -> receipt (happy path)" in {
      val entryTime = LocalDateTime.now().minusHours(2)
      val createJson = Json.obj("entryTime" -> entryTime.toString)

      // Create payment
      val createRequest = FakeRequest(POST, "/api/payments").withHeaders("Host" -> "127.0.0.1").withJsonBody(createJson)
      val createResult = route(app, createRequest).value
      status(createResult) mustBe CREATED
      val createdBody = contentAsJson(createResult)
      val id = (createdBody \ "id").as[Long]

      // Calculate fee
      val exitTime = LocalDateTime.now().minusHours(1)
      val calcJson = Json.obj("exitTime" -> exitTime.toString)
      val calcRequest = FakeRequest(POST, s"/api/payments/$id/calculate").withHeaders("Host" -> "127.0.0.1").withJsonBody(calcJson)
      val calcResult = route(app, calcRequest).value
      status(calcResult) mustBe OK

      // Process payment
      val procRequest = FakeRequest(POST, s"/api/payments/$id/process").withHeaders("Host" -> "127.0.0.1")
      val procResult = route(app, procRequest).value
      status(procResult) mustBe OK

      // Get receipt
      val receiptRequest = FakeRequest(GET, s"/api/payments/$id/receipt").withHeaders("Host" -> "127.0.0.1")
      val receiptResult = route(app, receiptRequest).value
      status(receiptResult) mustBe OK
      val receiptJson = contentAsJson(receiptResult)
      (receiptJson \ "paymentId").as[Long] mustBe id
      
      // Refund payment
      val refundRequest = FakeRequest(POST, s"/api/payments/$id/refund").withHeaders("Host" -> "127.0.0.1").withJsonBody(Json.obj())
      val refundResult = route(app, refundRequest).value
      status(refundResult) mustBe OK
      val refundJson = contentAsJson(refundResult)
      (refundJson \ "status").as[String] mustBe "REFUNDED"
    }
  }
}
