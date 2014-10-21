define(['./module', 'angular'], function(module, angular) {
  'use strict';

  module.service('SpecimenGroupService', SpecimenGroupService);

  SpecimenGroupService.$inject = ['biobankXhrReqService', 'domainEntityService'];

  /**
   * Service to access specimen groups.
   */
  function SpecimenGroupService(biobankXhrReqService, domainEntityService) {
    var service = {
      getAll                  : getAll,
      get                     : get,
      addOrUpdate             : addOrUpdate,
      remove                  : remove,
      anatomicalSourceTypes   : anatomicalSourceTypes,
      specimenTypes           : specimenTypes,
      preservTypes            : preservTypes,
      preservTempTypes        : preservTempTypes,
      specimenGroupValueTypes : specimenGroupValueTypes,
      specimenGroupIdsInUse   : specimenGroupIdsInUse
    };
    return service;

    //-------

    function getAll(studyId) {
      return biobankXhrReqService.call('GET', '/studies/sgroups/' + studyId);
    }

    function get(studyId, specimenGroupId) {
      return biobankXhrReqService.call('GET', '/studies/sgroups/' + studyId + '?sgId=' + specimenGroupId);
    }

    function addOrUpdate(specimenGroup) {
      var cmd = {
        studyId:                     specimenGroup.studyId,
        name:                        specimenGroup.name,
        units:                       specimenGroup.units,
        anatomicalSourceType:        specimenGroup.anatomicalSourceType,
        preservationType:            specimenGroup.preservationType,
        preservationTemperatureType: specimenGroup.preservationTemperatureType,
        specimenType:                specimenGroup.specimenType
      };

      angular.extend(cmd, domainEntityService.getOptionalAttribute(specimenGroup, 'description'));

      if (specimenGroup.id) {
        cmd.id = specimenGroup.id;
        cmd.expectedVersion = specimenGroup.version;
        return biobankXhrReqService.call('PUT', '/studies/sgroups/' + specimenGroup.id, cmd);
      } else {
        return biobankXhrReqService.call('POST', '/studies/sgroups', cmd);
      }
    }

    function remove(specimenGroup) {
      return biobankXhrReqService.call(
        'DELETE',
        '/studies/sgroups/' + specimenGroup.studyId + '/' + specimenGroup.id + '/' + specimenGroup.version);
    }

    function  anatomicalSourceTypes() {
      return biobankXhrReqService.call('GET', '/studies/anatomicalsrctypes');
    }

    function  specimenTypes() {
      return biobankXhrReqService.call('GET', '/studies/specimentypes');
    }

    function  preservTypes() {
      return biobankXhrReqService.call('GET', '/studies/preservtypes');
    }

    function  preservTempTypes() {
      return biobankXhrReqService.call('GET', '/studies/preservtemptypes');
    }

    function  specimenGroupValueTypes() {
      return biobankXhrReqService.call('GET', '/studies/sgvaluetypes');
    }

    function  specimenGroupIdsInUse(studyId) {
      return biobankXhrReqService.call('GET', '/studies/sgroups/inuse/' + studyId);
    }
  }

});
