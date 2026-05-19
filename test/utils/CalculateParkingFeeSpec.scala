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

    // After 15 minutes, we start charging ₹10 for each half hour slab.
    "charge one half hour slab for durations between 16 and 30 minutes" in {
      ParkingFeeCalculator.calculateParkingFee(16) mustBe BigDecimal(10)
      ParkingFeeCalculator.calculateParkingFee(30) mustBe BigDecimal(10)
    }

    // If you stay even a minute over the slab, you pay for the next half hour.
    "round up partial half hours to next slab" in {
      ParkingFeeCalculator.calculateParkingFee(31) mustBe BigDecimal(20)
      ParkingFeeCalculator.calculateParkingFee(59) mustBe BigDecimal(20)
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
    "exact daily cap boundary around 9.5/10 hours" in {
      ParkingFeeCalculator.calculateParkingFee(570) mustBe BigDecimal(190) // 19 half-hour slabs
      ParkingFeeCalculator.calculateParkingFee(571) mustBe BigDecimal(200) // rounds up to 20 slabs
      ParkingFeeCalculator.calculateParkingFee(600) mustBe BigDecimal(200) // 20 slabs
    }
  }
}
