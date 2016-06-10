/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  AnnotationTypeModalsFactory.$inject = [
    'modalService',
    'domainEntityService'
  ];

  /**
   *
   */
  function AnnotationTypeModalsFactory(modalService, domainEntityService) {

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
      var headerHtml = 'Cannot remove this annotation type';
      return modalService.modalOk(headerHtml, this.modalBodyHtml);
    };

    AnnotationTypeModals.prototype.remove = function (annotationType, removePromiseFunc) {
      return domainEntityService.removeEntity(
        removePromiseFunc,
        'Remove Annotation Type',
        'Are you sure you want to remove annotation type ' + annotationType.name + '?',
        'Remove failed',
        'Annotation type ' + annotationType.name + ' cannot be removed');
    };

    return AnnotationTypeModals;
  }

  return AnnotationTypeModalsFactory;
});
