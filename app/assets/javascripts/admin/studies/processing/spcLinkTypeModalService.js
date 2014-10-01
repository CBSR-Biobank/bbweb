define(['../../module'], function(module) {
  'use strict';

  module.service('spcLinkTypeModalService', spcLinkTypeModalService);

  spcLinkTypeModalService.$inject = ['$filter', 'domainEntityModalService', 'addTimeStamps'];

  /**
   * Displays a specimen link type in a modal. The information is displayed in an ng-table.
   */
  function spcLinkTypeModalService($filter, domainEntityModalService, addTimeStamps) {
    var service = {
      show: show
    };
    return service;

    //-------

    function show (spcLinkType, processingTypesById, specimenGropusById) {
      var title = 'Specimen Link Type';
      var data = [];
      var inputGroup =  specimenGropusById[spcLinkType.inputGroupId];
      var outputGroup =  specimenGropusById[spcLinkType.outputGroupId];

      data.push({name: 'Processing Type:',
                 value: processingTypesById[spcLinkType.processingTypeId].name});
      data.push({name: 'Input Group:', value: inputGroup.name});
      data.push({name: 'Expected input change:',
                 value: spcLinkType.expectedInputChange + ' ' + inputGroup.units});
      data.push({name: 'Input count:', value: spcLinkType.inputCount});
      data.push({name: 'Input Container Type:', value: 'None'});
      data.push({name: 'Output Group:', value: outputGroup.name});
      data.push({name: 'Expected output change:',
                 value: spcLinkType.expectedInputChange + ' ' + outputGroup.units});
      data.push({name: 'Output count:', value: spcLinkType.outputCount});
      data.push({name: 'Output Container Type:', value: 'None'});
      data = data.concat(addTimeStamps.get(spcLinkType));
      domainEntityModalService.show(title, data);
    }

  }

});
