/** Specimen Group helpers */
define(['./module'], function(module) {
  'use strict';

  /**
   * Displays a study in a modal.
   *
   */
  module.factory('StudyViewer', StudyViewerFactory);

  StudyViewerFactory.$inject = ['$filter', 'EntityViewer'];

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

});
