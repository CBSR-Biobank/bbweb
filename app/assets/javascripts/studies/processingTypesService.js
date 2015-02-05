define(['./module', 'angular'], function(module, angular) {
  'use strict';

  module.service('processingTypesService', ProcessingTypesService);

  ProcessingTypesService.$inject = ['biobankApi', 'domainEntityService',];

  /**
   * Service to access Processing Types.
   */
  function ProcessingTypesService(biobankApi, domainEntityService) {
    var service = {
      getAll      : getAll,
      get         : get,
      addOrUpdate : addOrUpdate,
      remove      : remove
    };
    return service;

    //-------

    function uri(studyId, ceventTypeId, version) {
      var result = '/studies';
      if (arguments.length <= 0) {
        throw new Error('study id not specified');
      } else {
        result += '/' + studyId + '/proctypes';

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
      return biobankApi.call('GET', uri(studyId));
    }

    function get(studyId, processingTypeId) {
      return biobankApi.call('GET', uri(studyId) + '?procTypeId=' + processingTypeId);
    }

    function addOrUpdate(processingType) {
      var cmd = {
        studyId:     processingType.studyId,
        name:        processingType.name,
        enabled:     processingType.enabled
      };

      angular.extend(cmd, domainEntityService.getOptionalAttribute(processingType, 'description'));

      if (processingType.id) {
        cmd.id = processingType.id;
        cmd.expectedVersion = processingType.version;
        return biobankApi.call('PUT', uri(processingType.studyId, processingType.id), cmd);
      } else {
        return biobankApi.call('POST', uri(processingType.studyId), cmd);
      }
    }

    function remove(processingType) {
      return biobankApi.call(
        'DELETE',
        uri(processingType.studyId, processingType.id, processingType.version));
    }
  }

});
