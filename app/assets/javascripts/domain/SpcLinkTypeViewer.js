define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.factory('SpcLinkTypeViewer', SpcLinkTypeViewerFactory);

  SpcLinkTypeViewerFactory.$inject = ['EntityViewer'];

  /**
   * Displays a specimen link type in a modal. The information is displayed in an ng-table.
   */
  function SpcLinkTypeViewerFactory(EntityViewer) {

    function SpcLinkTypeViewer(spcLinkType, processingTypesById, specimenGropusById, annotTypesById) {
      var inputGroup =  specimenGropusById[spcLinkType.inputGroupId];
      var outputGroup =  specimenGropusById[spcLinkType.outputGroupId];

      // FIXME move to domain object
      var atDataStrings = [];
      _.each(spcLinkType.annotationTypeData, function (atItem) {
        var annotType = annotTypesById[atItem.annotationTypeId];
        if (!annotType) {
          throw new Error('annotation type not found');
        }
        atDataStrings.push(annotType.name + (atItem.required ? ' (Req)' : ' (N/R)'));
      });

      var ev = new EntityViewer(spcLinkType, 'Specimen Link Type');

      ev.addAttribute('Processing Type:', processingTypesById[spcLinkType.processingTypeId].name);
      ev.addAttribute('Input Group:', inputGroup.name);
      ev.addAttribute('Expected input change:', spcLinkType.expectedInputChange + ' ' + inputGroup.units);
      ev.addAttribute('Input count:', spcLinkType.inputCount);
      ev.addAttribute('Input Container Type:',   'None');
      ev.addAttribute('Output Group:', outputGroup.name);
      ev.addAttribute('Expected output change:', spcLinkType.expectedInputChange + ' ' + outputGroup.units);
      ev.addAttribute('Output count:', spcLinkType.outputCount);
      ev.addAttribute('Output Container Type:', 'None');
      ev.addAttribute('Annotation Types:', atDataStrings.join(', '));

      ev.showModal();
    }

    return SpcLinkTypeViewer;

  }

});
