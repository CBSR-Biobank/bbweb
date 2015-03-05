define(['./module'], function(module) {
  'use strict';

  module.service('participantAnnotTypesService', participantAnnotTypesService);

  participantAnnotTypesService.$inject = [
    'StudyAnnotTypesService'
  ];

  /**
   * Service to access participant annotation types.
   */
  function participantAnnotTypesService(StudyAnnotTypesService) {

    function ParticipantAnnotTypesService() {
      StudyAnnotTypesService.call(this, 'pannottypes');
    }

    ParticipantAnnotTypesService.prototype = Object.create(StudyAnnotTypesService.prototype);

    return new ParticipantAnnotTypesService();
  }

});
