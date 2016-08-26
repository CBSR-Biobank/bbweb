/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  AnnotationTypeModalsFactory.$inject = [
    'gettext',
    'gettextCatalog',
    'modalService',
    'domainNotificationService'
  ];

  /**
   * Angular factory.
   */
  function AnnotationTypeModalsFactory(gettext,
                                       gettextCatalog,
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
      var headerHtml = gettext('Cannot remove this annotation type');
      return modalService.modalOk(headerHtml, this.modalBodyHtml);
    };

    AnnotationTypeModals.prototype.remove = function (annotationType, removePromiseFunc) {
      return domainNotificationService.removeEntity(
        removePromiseFunc,
        gettext('Remove Annotation Type'),
        gettext('Are you sure you want to remove annotation type {{name}}',
                { name: annotationType.name }),
        gettext('Remove failed'),
        gettext('Annotation type {{name}} cannot be removed', { name: annotationType.name }));
    };

    return AnnotationTypeModals;
  }

  return AnnotationTypeModalsFactory;
});
