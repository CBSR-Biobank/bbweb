package controllers

trait FormType {}
case class AddFormType() extends FormType
case class UpdateFormType() extends FormType

case class FormInfo(title: String, action: play.api.mvc.Call)