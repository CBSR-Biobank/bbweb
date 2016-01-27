/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
*/
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
      ev.addAttribute('Status',      study.getStatusLabel());

      ev.showModal();
    }

    return StudyViewer;
  }

  return StudyViewerFactory;
});
