define([], function() {
  'use strict';

  annotationTypeRemoveService.$inject = ['domainEntityRemoveService'];

  /**
   *
   */
  function annotationTypeRemoveService(domainEntityRemoveService) {
    var service = {
      remove : remove
    };
    return service;

    //-------

    function remove(removeFn, annotType, returnState) {
      domainEntityRemoveService.remove(
        'Remove Annotation Type',
        'Are you sure you want to remove annotation type ' + annotType.name + '?',
        'Annotation type ' + annotType.name + ' cannot be removed: ',
        removeFn,
        annotType,
        returnState
      );
    }

  }

  return annotationTypeRemoveService;
});
