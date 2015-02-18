define(['./module'], function(module) {
  'use strict';

  module.factory('CeventTypeViewer', CeventTypeViewerFactory);

  CeventTypeViewerFactory.$inject = ['EntityViewer'];

  /**
   * Displays a collection event type in a modal.
   *
   */
  function CeventTypeViewerFactory(EntityViewer) {

    /**
     *
     * @param {CollectionEventType} ceventType the JS object containing the information.
     */
    function CeventTypeViewer(study, ceventType) {
      var ev = new EntityViewer(ceventType, 'Collection Event Type');

      ev.addAttribute('Name:', ceventType.name);
      ev.addAttribute('Recurring:', ceventType.recurring ? 'Yes' : 'No');
      ev.addAttribute('Specimen Groups (Count, Amount):', ceventType.getSpecimenGroupsAsString());
      ev.addAttribute('Annotation Types:', ceventType.getAnnotationTypesAsString());
      ev.addAttribute('Description:', ceventType.description);

      ev.showModal();
    }

    return CeventTypeViewer;

  }

});
