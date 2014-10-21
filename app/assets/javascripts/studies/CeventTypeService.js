define(['./module', 'angular'], function(module, angular) {
  'use strict';

  module.service('CeventTypeService', CeventTypeService);

  CeventTypeService.$inject = ['biobankXhrReqService', 'domainEntityService'];

  /**
   * Service to access Collection Event Types.
   */
  function CeventTypeService(biobankXhrReqService, domainEntityService) {
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

    function addOrUpdate(ceventType) {
      var cmd = {
        studyId:            ceventType.studyId,
        name:               ceventType.name,
        recurring:          ceventType.recurring,
        specimenGroupData:  ceventType.specimenGroupData,
        annotationTypeData: ceventType.annotationTypeData
      };

      angular.extend(cmd, domainEntityService.getOptionalAttribute(ceventType, 'description'));

      if (ceventType.id) {
        cmd.id = ceventType.id;
        cmd.expectedVersion = ceventType.version;
        return biobankXhrReqService.call('PUT', '/studies/cetypes/' + ceventType.id, cmd);
      } else {
        return biobankXhrReqService.call('POST', '/studies/cetypes', cmd);
      }
    }

    function remove(ceventType) {
      return biobankXhrReqService.call(
        'DELETE',
        '/studies/cetypes/' + ceventType.studyId +
          '/' + ceventType.id +
          '/' + ceventType.version);
    }

  }

});
