/** Specimen Group helpers */
define(['./module'], function(module) {
  'use strict';

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  module.factory('SpecimenGroupViewer', SpecimenGroupViewerFactory);

  SpecimenGroupViewerFactory.$inject = ['EntityViewer'];

  function SpecimenGroupViewerFactory(EntityViewer) {

    function SpecimenGroupViewer(specimenGroup) {
      var ev = new EntityViewer(specimenGroup, 'Specimen Group');

      ev.addAttribute('Name:',                     specimenGroup.name);
      ev.addAttribute('Units:',                    specimenGroup.units);
      ev.addAttribute('Anatomical Source:',        specimenGroup.anatomicalSourceType);
      ev.addAttribute('Preservation Type:',        specimenGroup.preservationType);
      ev.addAttribute('Preservation Temperature:', specimenGroup.preservationTemperatureType);
      ev.addAttribute('Specimen Type:',            specimenGroup.specimenType);
      ev.addAttribute('Description:',              specimenGroup.description);

      ev.showModal();
    }

    return SpecimenGroupViewer;
  }

});
