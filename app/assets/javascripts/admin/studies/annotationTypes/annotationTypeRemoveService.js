define(['../../module'], function(module) {
  'use strict';

  module.service('annotationTypeRemoveService', annotationTypeRemoveService);

  annotationTypeRemoveService.$inject = ['modelObjRemoveService'];

  /**
   *
   */
  function annotationTypeRemoveService(modelObjRemoveService) {
    var service = {
      remove : remove
    };
    return service;

    //-------

    function remove(removeFn, annotType, returnState) {
      modelObjRemoveService.remove(
        'Remove Annotation Type',
        'Are you sure you want to remove annotation type ' + annotType.name + '?',
        'Annotation type ' + annotType.name + ' cannot be removed: ',
        removeFn,
        annotType,
        returnState
      );
    }

  }

});
