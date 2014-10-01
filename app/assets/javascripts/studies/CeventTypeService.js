define(['./module'], function(module) {
  'use strict';

  module.service('CeventTypeService', CeventTypeService);

  CeventTypeService.$inject = ['biobankXhrReqService'];

  /**
   * Service to access Collection Event Types.
   */
  function CeventTypeService(biobankXhrReqService) {
    var service = {
      getAll      : getAll,
      get         : get,
      addOrUpdate : addOrUpdate,
      remove      : remove
    };
    return service;

    //-------

    function getAll(studyId) {
      return biobankXhrReqService.call('GET', '/studies/cetypes/' + studyId);
    }

    function get(studyId, collectionEventTypeId) {
      return biobankXhrReqService.call('GET', '/studies/cetypes/' + studyId + '?cetId=' + collectionEventTypeId);
    }

    function addOrUpdate(collectionEventType) {
      var cmd = {
        studyId:            collectionEventType.studyId,
        name:               collectionEventType.name,
        description:        collectionEventType.description,
        recurring:          collectionEventType.recurring,
        specimenGroupData:  collectionEventType.specimenGroupData,
        annotationTypeData: collectionEventType.annotationTypeData
      };

      if (collectionEventType.id) {
        cmd.id = collectionEventType.id;
        cmd.expectedVersion = collectionEventType.version;
        return biobankXhrReqService.call('PUT', '/studies/cetypes/' + collectionEventType.id, cmd);
      } else {
        return biobankXhrReqService.call('POST', '/studies/cetypes', cmd);
      }
    }

    function remove(collectionEventType) {
      return biobankXhrReqService.call(
        'DELETE',
        '/studies/cetypes/' + collectionEventType.studyId +
          '/' + collectionEventType.id +
          '/' + collectionEventType.version);
    }

  }

});
