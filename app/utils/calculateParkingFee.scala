package utils

object ParkingFeeCalculator {

  def calculateParkingFee(durationMinutes: Long): BigDecimal = {
    val halfHourRate = BigDecimal(10)
    val dailyCap = BigDecimal(200)

    require(durationMinutes >= 0, "durationMinutes cannot be negative")
    if (durationMinutes <= 15) {
      BigDecimal(0)
    } else {
      val roundedUpHalfHours = Math.ceil(durationMinutes / 30.0).toLong
      val computedFee = BigDecimal(roundedUpHalfHours) * halfHourRate
      computedFee.min(dailyCap)
    }
  }
}
