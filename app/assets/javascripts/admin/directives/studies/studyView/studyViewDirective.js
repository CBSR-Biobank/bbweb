/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  /**
   *
   */
  function studyViewDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study: '='
      },
      templateUrl : '/assets/javascripts/admin/directives/studies/studyView/studyView.html',
      controller: StudyViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  StudyViewCtrl.$inject = ['$window', '$state', '$timeout'];

  function StudyViewCtrl($window, $state, $timeout) {
    var vm = this;

    vm.tabs = [
      { heading: 'Summary',      sref: 'home.admin.studies.study.summary',      active: true },
      { heading: 'Participants', sref: 'home.admin.studies.study.participants', active: false },
      { heading: 'Collection',   sref: 'home.admin.studies.study.collection',   active: false },
      { heading: 'Processing',   sref: 'home.admin.studies.study.processing',   active: false },
    ];

    init();

    //--

    function init() {
      activeTabUpdate();

      // initialize the panels to open state when viewing a new study
      if (vm.study.id !== $window.localStorage.getItem('study.panel.studyId')) {
        // this way when the user selects a new study, the panels always default to open
        $window.localStorage.setItem('study.panel.processingTypes',                true);
        $window.localStorage.setItem('study.panel.specimenLinkAnnotationTypes',    true);
        $window.localStorage.setItem('study.panel.specimenLinkTypes',              true);

        // remember the last viewed study
        $window.localStorage.setItem('study.panel.studyId', vm.study.id);
      }
    }

    function activeTabUpdate() {
      _.each(vm.tabs, function (tab, index) {
        tab.active = ($state.current.name.indexOf(tab.sref) >= 0);
        if (tab.active) {
          vm.active = index;
        }
      });
    }
  }

  return studyViewDirective;
});
