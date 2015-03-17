define([], function() {
  'use strict';

  StudyCtrl.$inject = ['$window', '$state', '$timeout', 'studiesService'];

  /**
   *
   */
  function StudyCtrl($window, $state, $timeout, studiesService) {
    var vm = this;
    vm.study = {};

    vm.tabSummaryActive      = false;
    vm.tabParticipantsActive = false;
    vm.tabSpecimensActive    = false;
    vm.tabCollectionActive   = false;
    vm.tabProcessingActive   = false;

    init();
    activeTabUpdateFix();

    //--

    function init() {
      studiesService.get($state.params.studyId).then(function (study) {
        vm.study = study;

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
      });
    }

    /**
     * At the moment the active tab does not initialize properly. Seems to be a ui-boostrap bug.
     *
     * See http://stackoverflow.com/questions/17695629/setting-the-initial-static-tab-in-angular-bootstrap
     */
    function activeTabUpdateFix() {
      $timeout(activeTabUpdate, 0);

      function activeTabUpdate() {
        vm.tabSummaryActive      = ($state.current.name === 'home.admin.studies.study.summary');
        vm.tabParticipantsActive = ($state.current.name === 'home.admin.studies.study.participants');
        vm.tabSpecimensActive    = ($state.current.name === 'home.admin.studies.study.specimens');
        vm.tabCollectionActive   = ($state.current.name === 'home.admin.studies.study.collection');
        vm.tabProcessingActive   = ($state.current.name === 'home.admin.studies.study.processing');
      }
    }

  }

  return StudyCtrl;
});
