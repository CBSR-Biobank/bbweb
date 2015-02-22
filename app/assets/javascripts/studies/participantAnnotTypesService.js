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

    ParticipantAnnotTypesService.prototype.getAll = function (studyId) {
      return StudyAnnotTypesService.prototype.getAll.call(this,
                                                          ParticipantAnnotationType,
                                                          studyId);
    };

    ParticipantAnnotTypesService.prototype.get = function (studyId, annotTypeId) {
      return StudyAnnotTypesService.prototype.get.call(this,
                                                       ParticipantAnnotationType,
                                                       studyId,
                                                       annotTypeId);
    };

    return new ParticipantAnnotTypesService();
  }

});
