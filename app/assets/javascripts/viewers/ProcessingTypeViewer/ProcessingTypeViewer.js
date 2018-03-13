/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * Displays a processing type in a modal.
 */
/* @ngInject */
function ProcessingTypeViewerFactory(EntityViewer) {

  function ProcessingTypeViewer(processingType) {
    var entityViewer = new EntityViewer(processingType, 'Processing Type');

    entityViewer.addAttribute('Name',        processingType.name);
    entityViewer.addAttribute('Description', processingType.description);
    entityViewer.addAttribute('Enabled',     processingType.enabled ? 'Yes' : 'No');

    entityViewer.showModal();
  }

  return ProcessingTypeViewer;
}

export default ngModule => ngModule.factory('ProcessingTypeViewer', ProcessingTypeViewerFactory)
