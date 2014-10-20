define(['./module'], function(module) {
  'use strict';

  module.service('ProcessingTypeService', ProcessingTypeService);

  ProcessingTypeService.$inject = ['biobankXhrReqService', 'domainEntityService',];

  /**
   * Service to access Processing Types.
   */
  function ProcessingTypeService(biobankXhrReqService, domainEntityService) {
    var service = {
      getAll      : getAll,
      get         : get,
      addOrUpdate : addOrUpdate,
      remove      : remove
    };
    return service;

    //-------
    function getAll(studyId) {
      return biobankXhrReqService.call('GET', '/studies/proctypes/' + studyId);
    }

    function get(studyId, processingTypeId) {
      return biobankXhrReqService.call('GET', '/studies/proctypes/' + studyId + '?procTypeId=' + processingTypeId);
    }

    function addOrUpdate(processingType) {
      var cmd = {
        studyId:     processingType.studyId,
        name:        processingType.name,
        enabled:     processingType.enabled
      };

      domainEntityService.getOptionalAttribute(cmd, processingType.description);

      if (processingType.id) {
        cmd.id = processingType.id;
        cmd.expectedVersion = processingType.version;
        return biobankXhrReqService.call('PUT', '/studies/proctypes/' + processingType.id, cmd);
      } else {
        return biobankXhrReqService.call('POST', '/studies/proctypes', cmd);
      }
    }

    function remove(processingType) {
      return biobankXhrReqService.call(
        'DELETE',
        '/studies/proctypes/' + processingType.studyId +
          '/' + processingType.id +
          '/' + processingType.version);
    }
  }

});
