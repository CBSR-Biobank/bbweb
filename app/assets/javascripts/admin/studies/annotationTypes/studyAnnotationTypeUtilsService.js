/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  studyAnnotationTypeUtils.$inject = [
    '$q',
    'modalService',
    'domainEntityService'
  ];

  /**
   *
   */
  function studyAnnotationTypeUtils($q,
                                    modalService,
                                    domainEntityService) {
    var service = {
      updateInUseModal: updateInUseModal,
      removeInUseModal: removeInUseModal,
      remove:     remove
    };
    return service;

    //-------

    function inUseModal(annotationType, type, action) {
      var headerHtml = 'Cannot ' + action + ' this annotation type',
          bodyHtml;

      if (type === 'ParticipantAnnotationType') {
        bodyHtml = 'This annotation type is in use by participants. ' +
          'If you want to make changes to the annotation type, ' +
          'it must first be removed from the participants that use it.';
      } else if (type === 'CollectionEventAnnotationType') {
        bodyHtml = 'This annotation type is in use by a collection event type. ' +
          'If you want to make changes to the annotation type, ' +
          'it must first be removed from the collection event type(s) that use it.';
      } else if (type === 'SpecimenLinkAnnotationType') {
        bodyHtml = 'This annotation type is in use by a specimen link type. ' +
          'If you want to make changes to the annotation type, ' +
          'it must first be removed from the specimen link type(s) that use it.';
      } else {
        throw new Error('invalid annotation type: ' + annotationType);
      }
      return modalService.modalOk(headerHtml, bodyHtml);
    }

    function updateInUseModal(annotationType, type) {
      return inUseModal(annotationType, type, 'update');
    }

    function removeInUseModal(annotationType, type) {
      return inUseModal(annotationType, type, 'remove');
    }

    function remove(removePromiseFunc, annotationType) {

      return domainEntityService.removeEntity(
        removePromiseFunc,
        'Remove Annotation Type',
        'Are you sure you want to remove annotation type ' + annotationType.name + '?',
        'Remove failed',
        'Annotation type ' + annotationType.name + ' cannot be removed');
    }
  }

  return studyAnnotationTypeUtils;
});
