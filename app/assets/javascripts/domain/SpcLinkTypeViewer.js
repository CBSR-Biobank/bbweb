define(['./module'], function(module) {
  'use strict';

  module.factory('SpcLinkTypeViewer', SpcLinkTypeViewerFactory);

  SpcLinkTypeViewerFactory.$inject = ['EntityViewer'];

  /**
   * Displays a specimen link type in a modal.
   */
  function SpcLinkTypeViewerFactory(EntityViewer) {

    function SpcLinkTypeViewer(spcLinkType) {
      var ev = new EntityViewer(spcLinkType, 'Specimen Link Type');

      ev.addAttribute('Processing Type:', spcLinkType.processingType.name);
      ev.addAttribute('Input Group:', spcLinkType.inputGroup.name);
      ev.addAttribute('Expected input change:', spcLinkType.expectedInputChange + ' ' + spcLinkType.inputGroup.units);
      ev.addAttribute('Input count:', spcLinkType.inputCount);
      ev.addAttribute('Input Container Type:',   'None');
      ev.addAttribute('Output Group:', spcLinkType.outputGroup.name);
      ev.addAttribute('Expected output change:', spcLinkType.expectedInputChange + ' ' + spcLinkType.outputGroup.units);
      ev.addAttribute('Output count:', spcLinkType.outputCount);
      ev.addAttribute('Output Container Type:', 'None');
      ev.addAttribute('Annotation Types:', spcLinkType.getAnnotationTypesAsString());

      ev.showModal();
    }

    return SpcLinkTypeViewer;

  }

});
