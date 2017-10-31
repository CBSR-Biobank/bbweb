/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 * Angular factory.
 */
/* @ngInject */
function AnnotationTypeModalsFactory(gettextCatalog,
                                     modalService,
                                     domainNotificationService) {

  /**
   * This is a base class and should not be used. Instead use one of the following:
   * <ul>
   *   <li>ParticipantAnnotationTypeModals</li>
   *   <li>CollectionEventAnnotationTypeModals</li>
   *   <li>SpecimenLinkAnnotationTypeModals</li>
   * </ul>
   */
  function AnnotationTypeModals(modalBodyHtml) {
    this.modalBodyHtml = modalBodyHtml;
  }

  AnnotationTypeModals.prototype.removeInUseModal = function () {
    var headerHtml = gettextCatalog.getString('Cannot remove this annotation type');
    return modalService.modalOk(headerHtml, this.modalBodyHtml);
  };

  AnnotationTypeModals.prototype.remove = function (annotationType, removePromiseFunc) {
    return domainNotificationService.removeEntity(
      removePromiseFunc,
      gettextCatalog.getString('Remove Annotation Type'),
      gettextCatalog.getString('Are you sure you want to remove annotation type {{name}}',
                               { name: annotationType.name }),
      gettextCatalog.getString('Remove failed'),
      gettextCatalog.getString('Annotation type {{name}} cannot be removed', { name: annotationType.name }));
  };

  return AnnotationTypeModals;
}

export default ngModule => ngModule.factory('AnnotationTypeModals', AnnotationTypeModalsFactory)
