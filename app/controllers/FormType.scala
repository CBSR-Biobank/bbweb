package controllers

/**
 * An input to a view containing a form that renders the form to either '''add'' or '''update'''
 * an object.
 */
trait FormType

/**
 * Used to render a form view to add an object.
 */
case class AddFormType() extends FormType

/**
 * Used to render a form view to update an object.
 */
case class UpdateFormType() extends FormType

/**
 * Used to store form parameters.
 *
 * @param title The title to display for the form. *
 * @param action The action to perform when the form is submitted.
 */
case class FormInfo(title: String, action: play.api.mvc.Call)