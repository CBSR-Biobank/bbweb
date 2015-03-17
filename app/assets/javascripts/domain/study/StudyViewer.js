/** Specimen Group helpers */
define([], function() {
  'use strict';

  StudyViewerFactory.$inject = ['$filter', 'EntityViewer'];

  /**
   * Displays a study in a modal.
   *
   */
  function StudyViewerFactory($filter, EntityViewer) {

    function StudyViewer(study) {
      var ev = new EntityViewer(study, 'Study');

      ev.addAttribute('Name',        study.name);
      ev.addAttribute('Description', $filter('truncate')(study.description, 60));
      ev.addAttribute('Status',      study.status);

      ev.showModal();
    }

    return StudyViewer;
  }

  return StudyViewerFactory;
});
