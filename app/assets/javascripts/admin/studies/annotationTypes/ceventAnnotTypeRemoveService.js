define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.service('ceventAnnotTypeRemoveService', ceventAnnotTypeRemoveService);

  ceventAnnotTypeRemoveService.$inject = [
    'annotationTypeRemoveService',
    'modalService',
    'ceventAnnotTypesService'
  ];

  /**
   * Removes a collection event annotation type.
   */
  function ceventAnnotTypeRemoveService(annotationTypeRemoveService,
                                        modalService,
                                        ceventAnnotTypesService) {
    var service = {
      remove: remove
    };
    return service;

    //-------

    function remove(annotType, annotTypesInUse) {
      if (_.contains(annotTypesInUse, annotType.id)) {
        var headerHtml = 'Cannot remove this annotation type';
        var bodyHtml = 'This annotation type is in use by a collection event type. ' +
            'If you want to remove the annotation type, ' +
            'it must first be removed from the collection event type(s) that use it.';
        modalService.modalOk(headerHtml, bodyHtml);
      } else {
        annotationTypeRemoveService.remove(
          ceventAnnotTypesService.remove,
          annotType,
          'admin.studies.study.collection',
          {studyId: annotType.studyId});
      }
    }
  }

});
