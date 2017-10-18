/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 * Displays a study annotation type in a modal.
 *
 */
/* @ngInject */
function SpecimenGroupViewerFactory(EntityViewer) {

  function SpecimenGroupViewer(specimenGroup) {
    var ev = new EntityViewer(specimenGroup, 'Specimen Group');

    ev.addAttribute('Name',                     specimenGroup.name);
    ev.addAttribute('Units',                    specimenGroup.units);
    ev.addAttribute('Anatomical Source',        specimenGroup.anatomicalSourceType);
    ev.addAttribute('Preservation Type',        specimenGroup.preservationType);
    ev.addAttribute('Preservation Temperature', specimenGroup.preservationTemperatureType);
    ev.addAttribute('Specimen Type',            specimenGroup.specimenType);
    ev.addAttribute('Description',              specimenGroup.description);

    ev.showModal();
  }

  return SpecimenGroupViewer;
}

export default ngModule => ngModule.factory('SpecimenGroupViewer', SpecimenGroupViewerFactory)
