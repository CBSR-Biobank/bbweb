define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.service('ceventAnnotTypeRemoveService', ceventAnnotTypeRemoveService);

  ceventAnnotTypeRemoveService.$inject = [
    'annotationTypeRemoveService',
    'modalService',
    'CeventAnnotTypeService'
  ];

  /**
   * Removes a collection event annotation type.
   */
  function ceventAnnotTypeRemoveService(annotationTypeRemoveService,
                                        modalService,
                                        CeventAnnotTypeService) {
    var service = {
      remove: remove
    };
    return service;

    //-------

    function remove(annotType, annotTypesInUse) {
      if (_.contains(annotTypesInUse, annotType.id)) {
        var headerText = 'Cannot remove this annotation type';
        var bodyText = 'This annotation type is in use by a collection event type. ' +
            'If you want to remove the annotation type, ' +
            'it must first be removed from the collection event type(s) that use it.';
        modalService.modalOk(headerText, bodyText);
      } else {
        annotationTypeRemoveService.remove(
          CeventAnnotTypeService.remove,
          annotType,
          'admin.studies.study.collection',
          {studyId: annotType.studyId});
      }
    }
  }

});
