define(['../../module'], function(module) {
  'use strict';

  /**
   * Removes a specimen link annotation type.
   */
  module.service('spcLinkAnnotTypeRemoveService', spcLinkAnnotTypeRemoveService);

  spcLinkAnnotTypeRemoveService.$inject = ['annotationTypeRemoveService', 'SpcLinkAnnotTypeService'];

  function spcLinkAnnotTypeRemoveService(annotationTypeRemoveService, SpcLinkAnnotTypeService) {
    var service = {
      remove: remove
    };
    return service;

    //-------

    function remove(annotType) {
      annotationTypeRemoveService.remove(
        SpcLinkAnnotTypeService.remove,
        annotType,
        'admin.studies.study.processing',
        {studyId: annotType.studyId});
    }
  }

});
