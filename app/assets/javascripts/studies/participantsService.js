define(['./module'], function(module) {
  'use strict';

  module.service('participantsService', participantsService);

  participantsService.$inject = ['biobankXhrReqService'];

  /**
   *
   */
  function participantsService(biobankXhrReqService) {
    var service = {
      get:         get,
      addOrUpdate: addOrUpdate,
      checkUnique: checkUnique
    };
    return service;

    //-------

    function uri(studyId, participantId) {
      var result = '/studies';
      if (arguments.length <= 0) {
        throw new Error('study id not specified');
      } else {
        result += '/' + studyId + '/participants';

        if (arguments.length > 1) {
          result += '/' + participantId;
        }
      }
      return result;
    }

    function get(studyId, participantId) {
      return biobankXhrReqService.call('GET', uri(studyId, participantId));
    }

    function addOrUpdate(participant) {
      var cmd = {
        studyId:     participant.studyId,
        uniqueId:    participant.uniqueId,
        annotations: participant.annotations
      };

      if (participant.id) {
        cmd.id = participant.id;
        cmd.expectedVersion = participant.version;
        return biobankXhrReqService.call('PUT', uri(participant.studyId, participant.id), cmd);
      } else {
        return biobankXhrReqService.call('POST', uri(participant.studyId), cmd);
      }
    }

    function checkUnique(uniqueId) {
      return biobankXhrReqService.call('GET', '/studies/participants/checkUnique/' + uniqueId);
    }
  }

});
