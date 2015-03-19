define(['underscore'], function(_) {
  'use strict';

  annotationTypeRemoveService.$inject = [
    '$q',
    'modalService',
    'StudyStatus'
  ];

  /**
   *
   */
  function annotationTypeRemoveService($q,
                                       modalService,
                                       StudyStatus) {
    var service = {
      remove : remove
    };
    return service;

    //-------

    function remove(annotType, study, studyAnnotationTypeIdsInUse) {
      var headerHtml, bodyHtml, deferred = $q.defer();

      if (study.status !== StudyStatus.DISABLED()) {
        throw new Error('study is not disabled');
      }

      if (_.contains(studyAnnotationTypeIdsInUse, annotType.id)) {
        headerHtml = 'Cannot remove this annotation type';
        bodyHtml = 'This annotation type is in use by participants. ' +
          'If you want to remove the annotation type, ' +
          'it must first be removed from the participants that use it.';
        modalService.modalOk(headerHtml, bodyHtml).then(deferred.reject);
      } else {
        headerHtml = 'Remove Annotation Type';
        bodyHtml = 'Are you sure you want to remove annotation type ' + annotType.name + '?';

        modalService.modalOk(headerHtml, bodyHtml).then(removeConfirmed);
      }

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

  return annotationTypeRemoveService;
});
