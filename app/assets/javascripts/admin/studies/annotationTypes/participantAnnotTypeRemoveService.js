define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.service('participantAnnotTypeRemoveService', participantAnnotTypeRemoveService);

  participantAnnotTypeRemoveService.$inject = [
    'annotationTypeRemoveService',
    'modalService',
    'ParticipantAnnotTypeService'
  ];

  /**
   * Removes a participant annotation type.
   */
  function participantAnnotTypeRemoveService(annotationTypeRemoveService,
                                             modalService,
                                             ParticipantAnnotTypeService) {
    var service = {
      remove: remove
    };
    return service;

    //-------

    function remove(annotType, annotTypesInUse) {
      if (_.contains(annotTypesInUse, annotType.id)) {
        var headerHtml = 'Cannot remove this annotation type';
        var bodyHtml = 'This annotation type is in use by participants. ' +
            'If you want to remove the annotation type, ' +
            'it must first be removed from the participants that use it.';
        modalService.modalOk(headerHtml, bodyHtml);
      } else {
        annotationTypeRemoveService.remove(
          ParticipantAnnotTypeService.remove,
          annotType,
          'admin.studies.study.participants');
      }
    }
  }

});
