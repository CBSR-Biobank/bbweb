define(['../../module', 'underscore'], function(module, _) {
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

    function show (spcLinkType, processingTypesById, specimenGropusById, annotTypesById) {
      var title = 'Specimen Link Type';
      var inputGroup =  specimenGropusById[spcLinkType.inputGroupId];
      var outputGroup =  specimenGropusById[spcLinkType.outputGroupId];

      var atDataStrings = [];
      _.each(spcLinkType.annotationTypeData, function (atItem) {
        var annotType = annotTypesById[atItem.annotationTypeId];
        if (!annotType) {
          throw new Error('annotation type not found');
        }
        atDataStrings.push(annotType.name + (atItem.required ? ' (Req)' : ' (N/R)'));
      });

      var data = [
        {name: 'Processing Type:',
         value: processingTypesById[spcLinkType.processingTypeId].name},
        {name: 'Input Group:', value: inputGroup.name},
        {name: 'Expected input change:',
         value: spcLinkType.expectedInputChange + ' ' + inputGroup.units},
        {name: 'Input count:', value: spcLinkType.inputCount},
        {name: 'Input Container Type:', value: 'None'},
        {name: 'Output Group:', value: outputGroup.name},
        {name: 'Expected output change:',
         value: spcLinkType.expectedInputChange + ' ' + outputGroup.units},
        {name: 'Output count:', value: spcLinkType.outputCount},
        {name: 'Output Container Type:', value: 'None'},
        {name: 'Annotation Types:', value: atDataStrings.join(', ')}
      ];
      data = data.concat(addTimeStamps.get(spcLinkType));
      domainEntityModalService.show(title, data);
    }

  }

});
