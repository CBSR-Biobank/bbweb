/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * Base class controller for components that allow adding annotation types.
 *
 * @memberOf  admin.common.controllers
 */
class AnnotationTypeAddController {

  /**
   * @param {object} $state - The UI Router state object.
   *
   * @param {service} notificationsService - The AngularJS service.
   *
   * @param {service} domainNotificationService - The AngularJS service.
   *
   * @param {string} domainObjTypeName - The name of the domain entity this annotation type belongs to.
   *
   * @param {function} addAnnotationTypePromiseFunc -
   *
   * @param {string} returnState - The state to return to when either the submit or cancel buttons are
   * pressed.
   */
  constructor($state,
              notificationsService,
              domainNotificationService,
              modalService,
              gettextCatalog,
              domainObjTypeName,
              returnState) {
    Object.assign(this, {
      $state,
      notificationsService,
      domainNotificationService,
      modalService,
      gettextCatalog,
      domainObjTypeName,
      returnState
    })
  }

  /**
   * This function should be overriden and is invoked to add the annotation type to the parent domain entity.
   * @abstract
   *
   * @param {domain.AnnotationType} annotationType - The annotation type to be persisted.
   */
  addAnnotationType(annotationType) { // eslint-disable-line no-unused-vars
    throw new Error('subclass should override this function')
  }

  /**
   * Used when the annotation type is ready to be sent to the server to persist it.
   * <p>
   * Usually called when the user presses the button to submit the form.
   *
   * @param {domain.AnnotationType} annotationType - The annotation type to be persisted.
   */
  submit(annotationType) {
    this.addAnnotationType(annotationType)
      .then(() => {
        this.notificationsService.submitSuccess();
        this.$state.go(this.returnState, {}, { reload: true });
      })
      .catch(error => {
        if (error.message.includes('EntityCriteriaError: annotation type name already used:')) {
          this.modalService.modalOk(
            this.gettextCatalog.getString('Annotation name error'),
            this.gettextCatalog.getString(
              'The name <b>{{name}}</b> is already in use for this event', { name: annotationType.name }))
            return;
        }
        this.domainNotificationService.updateErrorModal(error, this.domainObjTypeName)
      })
  }

  /**
   * Invokes the UI Router to change state due to the user pressing the **Cancel** button.
   */
  cancel() {
    this.$state.go(this.returnState, {}, { reload: true });
  }

}

// this controller is a base class and does not need to be included in AngularJS since it is imported by the
// controllers that extend it
export { AnnotationTypeAddController }
export default () => {}
