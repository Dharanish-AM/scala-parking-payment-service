package utils

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers

class CalculateParkingFeeSpec extends AnyWordSpec with Matchers {
  "ParkingFeeCalculator.calculateParkingFee" should {
    // First 15 minutes are free!
    "return 0 for duration <= 15 minutes" in {
      ParkingFeeCalculator.calculateParkingFee(0) mustBe BigDecimal(0)
      ParkingFeeCalculator.calculateParkingFee(15) mustBe BigDecimal(0)
    }

    // After 15 minutes, we start charging ₹20 for the first hour.
    "charge one hour for durations between 16 and 60 minutes" in {
      ParkingFeeCalculator.calculateParkingFee(16) mustBe BigDecimal(20)
      ParkingFeeCalculator.calculateParkingFee(60) mustBe BigDecimal(20)
    }

    // If you stay even a minute over the hour, you pay for the next full hour.
    "round up partial hours to next hour" in {
      ParkingFeeCalculator.calculateParkingFee(61) mustBe BigDecimal(40)
      ParkingFeeCalculator.calculateParkingFee(90) mustBe BigDecimal(40)
    }

    // The daily maximum charge is ₹200, no matter how long you stay.
    "apply the daily cap" in {
      ParkingFeeCalculator.calculateParkingFee(1000) mustBe BigDecimal(200)
    }

    // We can't have negative time, so this should error out.
    "throw IllegalArgumentException for negative durations" in {
      intercept[IllegalArgumentException] {
        ParkingFeeCalculator.calculateParkingFee(-5)
      }
    }

    // Checking exactly where the daily charge hits the ₹200 limit.
    "exact daily cap boundary around 9/10 hours" in {
      ParkingFeeCalculator.calculateParkingFee(540) mustBe BigDecimal(180) // 9 hours
      ParkingFeeCalculator.calculateParkingFee(541) mustBe BigDecimal(200) // rounds up to 10 hours
      ParkingFeeCalculator.calculateParkingFee(600) mustBe BigDecimal(200) // 10 hours
    }
  }
}
