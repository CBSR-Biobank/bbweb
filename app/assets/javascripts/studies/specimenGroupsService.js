define(['angular'], function(angular) {
  'use strict';

  specimenGroupsServiceFactory.$inject = ['biobankApi', 'domainEntityService'];

  /**
   * Service to access specimen groups.
   */
  function specimenGroupsServiceFactory(biobankApi, domainEntityService) {
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

    function uri(studyId, ceventTypeId, version) {
      var result = '/studies';
      if (arguments.length <= 0) {
        throw new Error('study id not specified');
      } else {
        result += '/' + studyId + '/sgroups';

        if (arguments.length > 1) {
          result += '/' + ceventTypeId;
        }

        if (arguments.length > 2) {
          result += '/' + version;
        }
      }
      return result;
    }

    function getAll(studyId) {
      return biobankApi.get(uri(studyId));
    }

    function get(studyId, specimenGroupId) {
      return biobankApi.get(uri(studyId) + '?sgId=' + specimenGroupId);
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
        return biobankApi.put(uri(specimenGroup.studyId, specimenGroup.id), cmd);
      } else {
        return biobankApi.post(uri(specimenGroup.studyId), cmd);
      }
    }

    function remove(specimenGroup) {
      return biobankApi.del(
        uri(specimenGroup.studyId, specimenGroup.id, specimenGroup.version));
    }

    function  specimenGroupIdsInUse(studyId) {
      return biobankApi.get(uri(studyId) + '/inuse');
    }

    function  anatomicalSourceTypes() {
      return biobankApi.get('/studies/anatomicalsrctypes');
    }

    function  specimenTypes() {
      return biobankApi.get('/studies/specimentypes');
    }

    function  preservTypes() {
      return biobankApi.get('/studies/preservtypes');
    }

    function  preservTempTypes() {
      return biobankApi.get('/studies/preservtemptypes');
    }

    function  specimenGroupValueTypes() {
      return biobankApi.get('/studies/sgvaluetypes');
    }
  }

  return specimenGroupsServiceFactory;
});
