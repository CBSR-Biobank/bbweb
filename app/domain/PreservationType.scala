package domain

object PreservationType extends Enumeration {
  type PreservationType = Value
  val FrozenSpecimen = Value("Frozen Specimen")
  val RnaLater = Value("RNA Later")
  val FreshSpecimen = Value("Fresh Specimen")
  val Slide = Value("Slide")
}

object PreservationTemperatureType extends Enumeration {
  type PreservationTemperatureType = Value
  val Plus4celcius = Value("4 C")
  val Minus20celcius = Value("-20 C")
  val Minus80celcius = Value("-80 C")
  val Minus180celcius = Value("-180 C")
  val RoomTemperature = Value("Room Temperature")
}
