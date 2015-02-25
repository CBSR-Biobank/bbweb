define(['./module'], function(module) {
  'use strict';

  module.service('participantAnnotTypesService', participantAnnotTypesService);

  participantAnnotTypesService.$inject = [
    'StudyAnnotTypesService',
    'ParticipantAnnotationType'
  ];

  /**
   * Service to access participant annotation types.
   */
  function participantAnnotTypesService(StudyAnnotTypesService,
                                        ParticipantAnnotationType) {

    function ParticipantAnnotTypesService() {
      StudyAnnotTypesService.call(this, 'pannottypes');
    }

    ParticipantAnnotTypesService.prototype = Object.create(StudyAnnotTypesService.prototype);

    return new ParticipantAnnotTypesService();
  }

});
