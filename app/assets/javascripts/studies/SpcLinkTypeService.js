define(['./module', 'angular'], function(module, angular) {
  'use strict';

  module.service('SpcLinkTypeService', SpcLinkTypeService);

  SpcLinkTypeService.$inject = ['biobankXhrReqService', 'domainEntityService'];

  /**
   * Service to access Spcecimen Link Types.
   */
  function SpcLinkTypeService(biobankXhrReqService, domainEntityService) {
    var service = {
      getAll      : getAll,
      get         : get,
      addOrUpdate : addOrUpdate,
      remove      : remove
    };
    return service;

    //-------
    function getAll(studyId) {
      return biobankXhrReqService.call('GET', '/studies/sltypes/' + studyId);
    }

    function get(studyId, spcLinkTypeId) {
      return biobankXhrReqService.call('GET', '/studies/sltypes/' + studyId + '?slTypeId=' + spcLinkTypeId);
    }

    function addOrUpdate(spcLinkType) {
      var cmd = {
        processingTypeId:      spcLinkType.processingTypeId,
        expectedInputChange:   spcLinkType.expectedInputChange,
        expectedOutputChange:  spcLinkType.expectedOutputChange,
        inputCount:            spcLinkType.inputCount,
        outputCount:           spcLinkType.outputCount,
        inputGroupId:          spcLinkType.inputGroupId,
        outputGroupId:         spcLinkType.outputGroupId,
        inputContainerTypeId:  spcLinkType.inputContainerTypeId,
        outputContainerTypeId: spcLinkType.outputContainerTypeId
      };

      angular.extend(cmd, domainEntityService.getOptionalAttribute(spcLinkType, 'annotationTypeData'));

      if (spcLinkType.id) {
        cmd.id = spcLinkType.id;
        cmd.expectedVersion = spcLinkType.version;
        return biobankXhrReqService.call('PUT', '/studies/sltypes/' + spcLinkType.id, cmd);
      } else {
        return biobankXhrReqService.call('POST', '/studies/sltypes', cmd);
      }
    }

    function remove(spcLinkType) {
      return biobankXhrReqService.call(
        'DELETE',
        '/studies/sltypes/' + spcLinkType.processingTypeId +
          '/' + spcLinkType.id +
          '/' + spcLinkType.version);
    }

  }

});
