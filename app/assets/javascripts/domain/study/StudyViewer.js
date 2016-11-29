/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  StudyViewerFactory.$inject = ['$filter', 'EntityViewer', 'gettextCatalog'];

  /**
   * Displays a study in a modal.
   *
   */
  function StudyViewerFactory($filter, EntityViewer, gettextCatalog) {

    function StudyViewer(study) {
      var ev = new EntityViewer(study, 'Study');

      ev.addAttribute(gettextCatalog.getString('Name'),        study.name);
      ev.addAttribute(gettextCatalog.getString('Description'), $filter('truncate')(study.description, 60));
      ev.addAttribute(gettextCatalog.getString('State'),      study.state.toUpperCase());

      ev.showModal();
    }

    return StudyViewer;
  }

  return StudyViewerFactory;
});
