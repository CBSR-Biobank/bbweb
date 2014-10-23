define(['./module'], function(module) {
  'use strict';

  module.service('spcLinkTypesService', SpcLinkTypesService);

  SpcLinkTypesService.$inject = ['biobankXhrReqService'];

  /**
   * Service to access Spcecimen Link Types.
   */
  function SpcLinkTypesService(biobankXhrReqService) {
    var service = {
      getAll      : getAll,
      get         : get,
      addOrUpdate : addOrUpdate,
      remove      : remove
    };
    return service;

    //-------

    function uri(processingTypeId, ceventTypeId, version) {
      var result = '/studies';
      if (arguments.length <= 0) {
        throw new Error('study id not specified');
      } else {
        result += '/' + processingTypeId + '/sltypes';

        if (arguments.length > 1) {
          result += '/' + ceventTypeId;
        }

        if (arguments.length > 2) {
          result += '/' + version;
        }
      }
      return result;
    }

    function getAll(processingTypeId) {
      return biobankXhrReqService.call('GET', uri(processingTypeId));
    }

    function get(processingTypeId, spcLinkTypeId) {
      return biobankXhrReqService.call('GET', uri(processingTypeId) + '?slTypeId=' + spcLinkTypeId);
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
        outputContainerTypeId: spcLinkType.outputContainerTypeId,
        annotationTypeData:    spcLinkType.annotationTypeData
      };

      if (spcLinkType.id) {
        cmd.id = spcLinkType.id;
        cmd.expectedVersion = spcLinkType.version;
        return biobankXhrReqService.call('PUT', uri(spcLinkType.processingTypeId, spcLinkType.id), cmd);
      } else {
        return biobankXhrReqService.call('POST', uri(spcLinkType.processingTypeId), cmd);
      }
    }

    function remove(spcLinkType) {
      return biobankXhrReqService.call(
        'DELETE',
        uri(spcLinkType.processingTypeId, spcLinkType.id, spcLinkType.version));
    }

  }

});
