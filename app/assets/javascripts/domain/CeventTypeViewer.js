define(['./module'], function(module) {
  'use strict';

  module.factory('CeventTypeViewer', CeventTypeViewerFactory);

  CeventTypeViewerFactory.$inject = [
    'EntityViewer',
    'CollectionEventType',
    'SpecimenGroupSet',
    'AnnotationTypeSet'
  ];

  /**
   * Displays a collection event type in a modal.
   */
  function CeventTypeViewerFactory(EntityViewer,
                                   CollectionEventType,
                                   SpecimenGroupSet,
                                   AnnotationTypeSet) {

    function CeventTypeViewer(study, ceventType, specimenGroups, annotTypes) {
      var specimenGroupSet  = new SpecimenGroupSet(specimenGroups);
      var annotationTypeSet  = new AnnotationTypeSet(annotTypes);
      var cet = new CollectionEventType(study, ceventType, specimenGroupSet, annotationTypeSet);
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
