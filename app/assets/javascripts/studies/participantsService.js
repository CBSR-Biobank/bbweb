define(['./module'], function(module) {
  'use strict';

  module.service('participantsService', participantsService);

  participantsService.$inject = ['biobankApi'];

  /**
   *
   */
  function participantsService(biobankApi) {
    var service = {
      get:           get,
      getByUniqueId: getByUniqueId,
      addOrUpdate:   addOrUpdate
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
      return biobankApi.call('GET', uri(studyId, participantId));
    }

    function getByUniqueId(studyId, uniqueId) {
      return biobankApi.call('GET', uri(studyId) + '/uniqueId/' + uniqueId);
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
        return biobankApi.call('PUT', uri(participant.studyId, participant.id), cmd);
      } else {
        return biobankApi.call('POST', uri(participant.studyId), cmd);
      }
    }
  }

});
