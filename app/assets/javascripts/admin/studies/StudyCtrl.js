define(['../module'], function(module) {
  'use strict';

  module.controller('StudyCtrl', StudyCtrl);

  StudyCtrl.$inject = ['$window', '$scope', '$state', '$timeout', 'study'];

  /**
   *
   */
  function StudyCtrl($window, $scope, $state, $timeout, study) {
    var vm = this;
    vm.study = study;

    vm.tabSummaryActive      = false;
    vm.tabParticipantsActive = false;
    vm.tabSpecimensActive    = false;
    vm.tabCollectionActive   = false;
    vm.tabProcessingActive   = false;

    panelStateInit();
    activeTabUpdateFix();

    //--

    // initialize the panels to open state when viewing a new study
    function panelStateInit() {
      if (study.id !== $window.localStorage.getItem('study.panel.studyId')) {
        // this way when the user selects a new study, the panels always default to open
        $window.localStorage.setItem('study.panel.collectionEventTypes',        true);
        $window.localStorage.setItem('study.panel.participantAnnotationTypes',  true);
        $window.localStorage.setItem('study.panel.participantAnnottionTypes',   true);
        $window.localStorage.setItem('study.panel.processingTypes',             true);
        $window.localStorage.setItem('study.panel.specimenGroups',              true);
        $window.localStorage.setItem('study.panel.specimenLinkAnnotationTypes', true);
        $window.localStorage.setItem('study.panel.specimenLinkTypes',           true);

        // remember the last viewed study
        $window.localStorage.setItem('study.panel.studyId', study.id);
      }
    }

    function activeTabUpdateFix() {
      $timeout(activeTabUpdate, 0);

      function activeTabUpdate() {
        vm.tabSummaryActive      = ($state.current.name === 'admin.studies.study.summary');
        vm.tabParticipantsActive = ($state.current.name === 'admin.studies.study.participants');
        vm.tabSpecimensActive    = ($state.current.name === 'admin.studies.study.specimens');
        vm.tabCollectionActive   = ($state.current.name === 'admin.studies.study.collection');
        vm.tabProcessingActive   = ($state.current.name === 'admin.studies.study.processing');
      }
    }

  }

});
