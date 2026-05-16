package utils

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers

class CalculateParkingFeeSpec extends AnyWordSpec with Matchers {
  "calculateParkingFee.calculateParkingFee" should {
    // First 15 minutes are free!
    "return 0 for duration <= 15 minutes" in {
      calculateParkingFee.calculateParkingFee(0) mustBe BigDecimal(0)
      calculateParkingFee.calculateParkingFee(15) mustBe BigDecimal(0)
    }

    // After 15 minutes, we start charging ₹20 for the first hour.
    "charge one hour for durations between 16 and 60 minutes" in {
      calculateParkingFee.calculateParkingFee(16) mustBe BigDecimal(20)
      calculateParkingFee.calculateParkingFee(60) mustBe BigDecimal(20)
    }

    // If you stay even a minute over the hour, you pay for the next full hour.
    "round up partial hours to next hour" in {
      calculateParkingFee.calculateParkingFee(61) mustBe BigDecimal(40)
      calculateParkingFee.calculateParkingFee(90) mustBe BigDecimal(40)
    }

    // The daily maximum charge is ₹200, no matter how long you stay.
    "apply the daily cap" in {
      calculateParkingFee.calculateParkingFee(1000) mustBe BigDecimal(200)
    }

    // We can't have negative time, so this should error out.
    "throw IllegalArgumentException for negative durations" in {
      intercept[IllegalArgumentException] {
        calculateParkingFee.calculateParkingFee(-5)
      }
    }

    // Checking exactly where the daily charge hits the ₹200 limit.
    "exact daily cap boundary around 9/10 hours" in {
      calculateParkingFee.calculateParkingFee(540) mustBe BigDecimal(180) // 9 hours
      calculateParkingFee.calculateParkingFee(541) mustBe BigDecimal(200) // rounds up to 10 hours
      calculateParkingFee.calculateParkingFee(600) mustBe BigDecimal(200) // 10 hours
    }
  }
}
