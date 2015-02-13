define(['./module'], function(module) {
  'use strict';

  module.factory('CeventTypeViewer', CeventTypeViewerFactory);

  CeventTypeViewerFactory.$inject = ['EntityViewer', 'CollectionEventType'];

  /**
   * Displays a collection event type in a modal. The information is displayed in an ng-table.
   */
  function CeventTypeViewerFactory(EntityViewer, CollectionEventType) {

    function CeventTypeViewer(study, ceventType, specimenGroups, annotTypes) {
      var cet = new CollectionEventType(study, ceventType, specimenGroups, annotTypes);
      var ev = new EntityViewer(cet, 'Collection Event Type');

      ev.addAttribute('Name:', ceventType.name);
      ev.addAttribute('Recurring:', ceventType.recurring ? 'Yes' : 'No');
      ev.addAttribute('Specimen Groups (Count, Amount):', cet.getSpecimenGroupsAsString());
      ev.addAttribute('Annotation Types:', cet.getAnnotationTypesAsString());
      ev.addAttribute('Description:', ceventType.description);

      ev.showModal();
    }

    return CeventTypeViewer;

  }

});
