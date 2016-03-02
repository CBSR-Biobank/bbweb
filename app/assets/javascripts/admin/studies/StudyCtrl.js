/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  StudyCtrl.$inject = ['$window', '$state', '$timeout', 'study'];

  /**
   *
   */
  function StudyCtrl($window, $state, $timeout, study) {
    var vm = this;

    vm.study                 = study;
    vm.tabSummaryActive      = false;
    vm.tabParticipantsActive = false;
    vm.tabSpecimensActive    = false;
    vm.tabCollectionActive   = false;
    vm.tabProcessingActive   = false;

    init();
    activeTabUpdateFix();

    //--

    function init() {
      // initialize the panels to open state when viewing a new study
      if (vm.study.id !== $window.localStorage.getItem('study.panel.studyId')) {
        // this way when the user selects a new study, the panels always default to open
        $window.localStorage.setItem('study.panel.participantAnnotationTypes',     true);
        $window.localStorage.setItem('study.panel.specimenGroups',                 true);
        $window.localStorage.setItem('study.panel.collectionEventTypes',           true);
        $window.localStorage.setItem('study.panel.collectionEventAnnotationTypes', true);
        $window.localStorage.setItem('study.panel.processingTypes',                true);
        $window.localStorage.setItem('study.panel.specimenLinkAnnotationTypes',    true);
        $window.localStorage.setItem('study.panel.specimenLinkTypes',              true);

        // remember the last viewed study
        $window.localStorage.setItem('study.panel.studyId', vm.study.id);
      }
    }

    /**
     * At the moment the active tab does not initialize properly. Seems to be a ui-boostrap bug.
     *
     * See http://stackoverflow.com/questions/17695629/setting-the-initial-static-tab-in-angular-bootstrap
     */
    function activeTabUpdateFix() {
      $timeout(activeTabUpdate, 0);

      function activeTabUpdate() {
        vm.tabSummaryActive      = ($state.current.name.startsWith('home.admin.studies.study.summary'));
        vm.tabParticipantsActive = ($state.current.name.startsWith('home.admin.studies.study.participants'));
        vm.tabSpecimensActive    = ($state.current.name.startsWith('home.admin.studies.study.specimens'));
        vm.tabCollectionActive   = ($state.current.name.startsWith('home.admin.studies.study.collection'));
        vm.tabProcessingActive   = ($state.current.name.startsWith('home.admin.studies.study.processing'));
      }
    }

  }

  return StudyCtrl;
});
