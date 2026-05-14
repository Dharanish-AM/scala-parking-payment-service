package utils

object calculateParkingFee {
  def calculateParkingFee(durationMinutes: Long): BigDecimal = {
    val hourlyRate = BigDecimal(20)
    val dailyCap = BigDecimal(200)

    if (durationMinutes <= 15) {
      BigDecimal(0)
    } else {
      val roundedUpHours = Math.ceil(durationMinutes / 60.0).toLong
      val computedFee = BigDecimal(roundedUpHours) * hourlyRate
      computedFee.min(dailyCap)
    }
  }
}
