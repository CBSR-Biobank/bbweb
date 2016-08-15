package org.biobank.infrastructure

import play.api.libs.json._
import play.api.libs.json.JsString

object EnumUtils {
  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] =
    new Reads[E#Value] {
      def reads(json: JsValue): JsResult[E#Value] = json match {
        case JsString(s) => {
          try {
            JsSuccess[E#Value](enum.withName(s))
          } catch {
            case _: NoSuchElementException =>
               JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not appear to contain the value: '$s'")
          }
        }
        case _ => JsError("String value expected")
      }
  }

  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] =
    new Writes[E#Value] {
      @SuppressWarnings(Array("org.wartremover.warts.ToString"))
      def writes(v: E#Value): JsValue = JsString(v.toString)
    }

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(enumReads(enum), enumWrites)
  }
}
