define(['../../module'], function(module) {
  'use strict';

  module.service('ceventAnnotTypeRemoveService', ceventAnnotTypeRemoveService);

  ceventAnnotTypeRemoveService.$inject = ['annotationTypeRemoveService', 'CeventAnnotTypeService'];

  /**
   * Removes a collection event annotation type.
   */
  function ceventAnnotTypeRemoveService(annotationTypeRemoveService, CeventAnnotTypeService) {
    var service = {
      remove: remove
    };
    return service;

    //-------

    function remove(annotType) {
      annotationTypeRemoveService.remove(
        CeventAnnotTypeService.remove,
        annotType,
        'admin.studies.study.collection',
        {studyId: annotType.studyId});
    }
  }

});
