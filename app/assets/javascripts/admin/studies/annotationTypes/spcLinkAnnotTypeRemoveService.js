define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  /**
   * Removes a specimen link annotation type.
   */
  module.service('spcLinkAnnotTypeRemoveService', spcLinkAnnotTypeRemoveService);

  spcLinkAnnotTypeRemoveService.$inject = [
    'annotationTypeRemoveService',
    'modalService',
    'SpcLinkAnnotTypeService'
  ];

  function spcLinkAnnotTypeRemoveService(annotationTypeRemoveService,
                                         modalService,
                                         SpcLinkAnnotTypeService) {
    var service = {
      remove: remove
    };
    return service;

    //-------

    function remove(annotType, annotTypesInUse) {
      if (_.contains(annotTypesInUse, annotType.id)) {
        var headerText = 'Cannot remove this annotation type';
        var bodyText = 'This annotation type is in use by a specimen link type. ' +
            'If you want to remove the annotation type, ' +
            'it must first be removed from the speicmen link type(s) that use it.';
        modalService.modalOk(headerText, bodyText);
      } else {
        annotationTypeRemoveService.remove(
          SpcLinkAnnotTypeService.remove,
          annotType,
          'admin.studies.study.processing',
          {studyId: annotType.studyId});
      }
    }
  }

});
