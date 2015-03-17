define([], function() {
  'use strict';

  participantAnnotTypesServiceFactory.$inject = [
    'StudyAnnotTypesService'
  ];

  /**
   * Service to access participant annotation types.
   */
  function participantAnnotTypesServiceFactory(StudyAnnotTypesService) {

    function ParticipantAnnotTypesService() {
      StudyAnnotTypesService.call(this, 'pannottypes');
    }

    ParticipantAnnotTypesService.prototype = Object.create(StudyAnnotTypesService.prototype);

    return new ParticipantAnnotTypesService();
  }

  return participantAnnotTypesServiceFactory;
});
