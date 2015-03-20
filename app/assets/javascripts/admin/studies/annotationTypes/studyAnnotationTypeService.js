define(['underscore'], function(_) {
  'use strict';

  studyAnnotationTypeService.$inject = [
    '$q',
    'modalService',
    'ParticipantAnnotationType',
    'CollectionEventAnnotationType',
    'SpecimenLinkAnnotationType'
  ];

  /**
   *
   */
  function studyAnnotationTypeService($q,
                                      modalService,
                                      ParticipantAnnotationType,
                                      CollectionEventAnnotationType,
                                      SpecimenLinkAnnotationType) {
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

    function remove(annotType, study) {
      var headerHtml, bodyHtml, deferred = $q.defer();

      if (!study.isDisabled()) {
        throw new Error('study is not disabled');
      }

      headerHtml = 'Remove Annotation Type';
      bodyHtml = 'Are you sure you want to remove annotation type ' + annotType.name + '?';
      modalService.modalOk(headerHtml, bodyHtml).then(removeConfirmed);
      return deferred.promise;

      function removeConfirmed() {
        return annotType.remove()
          .then(deferred.resolve)
          .catch(function (error) {
            var modalOptions = {
              closeButtonText: 'Cancel',
              headerHtml: 'Remove failed',
              bodyHtml: 'Annotation type ' + annotType.name + ' cannot be removed: ' + error
            };
            modalService.showModal({}, modalOptions).then(deferred.reject);
          });
      }
    }
  }

  return studyAnnotationTypeService;
});
