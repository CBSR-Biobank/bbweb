define(['underscore'], function(_) {
  'use strict';

  studyAnnotationTypeUtils.$inject = [
    '$q',
    'modalService',
    'ParticipantAnnotationType',
    'CollectionEventAnnotationType',
    'SpecimenLinkAnnotationType',
    'domainEntityService'
  ];

  /**
   *
   */
  function studyAnnotationTypeUtils($q,
                                    modalService,
                                    ParticipantAnnotationType,
                                    CollectionEventAnnotationType,
                                    SpecimenLinkAnnotationType,
                                    domainEntityService) {
    var service = {
      inUseModal: inUseModal,
      remove:     remove
    };
    return service;

    //-------

    function inUseModal(annotationType) {
      var headerHtml = 'Cannot update this annotation type',
          bodyHtml;

      if (annotationType instanceof ParticipantAnnotationType) {
        bodyHtml = 'This annotation type is in use by participants. ' +
          'If you want to make changes to the annotation type, ' +
          'it must first be removed from the participants that use it.';
      } else if (annotationType instanceof CollectionEventAnnotationType) {
        bodyHtml = 'This annotation type is in use by a collection event type. ' +
          'If you want to make changes to the annotation type, ' +
          'it must first be removed from the collection event type(s) that use it.';
      } else if (annotationType instanceof SpecimenLinkAnnotationType) {
        bodyHtml = 'This annotation type is in use by a specimen link type. ' +
          'If you want to make changes to the annotation type, ' +
          'it must first be removed from the specimen link type(s) that use it.';
      } else {
        throw new Error('invalid annotation type: ' + annotationType);
      }
      return modalService.modalOk(headerHtml, bodyHtml);
    }

    function remove(annotationType) {

      function removeInternal() {
        return annotationType.remove();
      }

      return domainEntityService.removeEntity(
        removeInternal,
        'Remove Annotation Type',
        'Are you sure you want to remove annotation type ' + annotationType.name + '?',
        'Remove failed',
        'Annotation type ' + annotationType.name + ' cannot be removed');
    }
  }

  return studyAnnotationTypeUtils;
});
