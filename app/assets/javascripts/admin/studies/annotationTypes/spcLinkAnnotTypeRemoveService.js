define(['underscore'], function(_) {
  'use strict';

  spcLinkAnnotTypeRemoveService.$inject = [
    'annotationTypeRemoveService',
    'modalService',
    'spcLinkAnnotTypesService'
  ];

  /**
   * Removes a specimen link annotation type.
   */
  function spcLinkAnnotTypeRemoveService(annotationTypeRemoveService,
                                         modalService,
                                         spcLinkAnnotTypesService) {
    var service = {
      remove: remove
    };
    return service;

    //-------

    function remove(annotType, annotTypesInUse) {
      if (_.contains(annotTypesInUse, annotType.id)) {
        var headerHtml = 'Cannot remove this annotation type';
        var bodyHtml = 'This annotation type is in use by a specimen link type. ' +
            'If you want to remove the annotation type, ' +
            'it must first be removed from the speicmen link type(s) that use it.';
        modalService.modalOk(headerHtml, bodyHtml);
      } else {
        annotationTypeRemoveService.remove(
          spcLinkAnnotTypesService.remove,
          annotType,
          'home.admin.studies.study.processing',
          {studyId: annotType.studyId});
      }
    }
  }

  return spcLinkAnnotTypeRemoveService;
});
