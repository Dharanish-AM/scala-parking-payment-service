package utils

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers

class CalculateParkingFeeSpec extends AnyWordSpec with Matchers {
  "calculateParkingFee.calculateParkingFee" should {
    "return 0 for duration <= 15 minutes" in {
      calculateParkingFee.calculateParkingFee(0) mustBe BigDecimal(0)
      calculateParkingFee.calculateParkingFee(15) mustBe BigDecimal(0)
    }

    "charge one hour for durations between 16 and 60 minutes" in {
      calculateParkingFee.calculateParkingFee(16) mustBe BigDecimal(20)
      calculateParkingFee.calculateParkingFee(60) mustBe BigDecimal(20)
    }

    "round up partial hours to next hour" in {
      calculateParkingFee.calculateParkingFee(61) mustBe BigDecimal(40)
      calculateParkingFee.calculateParkingFee(90) mustBe BigDecimal(40)
    }

    "apply the daily cap" in {
      calculateParkingFee.calculateParkingFee(1000) mustBe BigDecimal(200)
    }
  }
}
