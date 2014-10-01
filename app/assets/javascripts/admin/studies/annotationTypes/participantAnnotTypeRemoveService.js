define(['../../module'], function(module) {
  'use strict';

  module.service('participantAnnotTypeRemoveService', participantAnnotTypeRemoveService);

  participantAnnotTypeRemoveService.$inject = ['annotationTypeRemoveService', 'ParticipantAnnotTypeService'];

  /**
   * Removes a participant annotation type.
   */
  function participantAnnotTypeRemoveService(annotationTypeRemoveService, ParticipantAnnotTypeService) {
    var service = {
      remove: remove
    };
    return service;

    //-------

    function remove(annotType) {
      annotationTypeRemoveService.remove(
        ParticipantAnnotTypeService.remove,
        annotType,
        'admin.studies.study.participants');
    }
  }

});
