/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
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
      templateUrl : '/assets/javascripts/admin/studies/directives/studyView/studyView.html',
      controller: StudyViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  StudyViewCtrl.$inject = ['$scope', '$window', '$state'];

  function StudyViewCtrl($scope, $window, $state) {
    var vm = this;

    vm.tabs = [
      { heading: 'Summary',      sref: 'home.admin.studies.study.summary',      active: true },
      { heading: 'Participants', sref: 'home.admin.studies.study.participants', active: false },
      { heading: 'Collection',   sref: 'home.admin.studies.study.collection',   active: false },
      { heading: 'Processing',   sref: 'home.admin.studies.study.processing',   active: false }
    ];
    vm.activeTabUpdate = activeTabUpdate;

    init();

    //--

    /**
     * Initialize the panels to open state when viewing a new study.
     */
    function init() {
      $scope.$on('study-view', activeTabUpdate);

      if (vm.study.id !== $window.localStorage.getItem('study.panel.studyId')) {
        // this way when the user selects a new study, the panels always default to open
        $window.localStorage.setItem('study.panel.processingTypes',                true);
        $window.localStorage.setItem('study.panel.specimenLinkAnnotationTypes',    true);
        $window.localStorage.setItem('study.panel.specimenLinkTypes',              true);

        // remember the last viewed study
        $window.localStorage.setItem('study.panel.studyId', vm.study.id);
      }
    }

    /**
     * Updates the selected tab.
     *
     * This function is called when event 'study-view' is emitted by child scopes.
     *
     * This event is emitted by the following child directives:
     * <ul>
     *   <li>studySummaryDirective</li>
     *   <li>studyParticipantsTabDirective</li>
     *   <li>studyCollectionDirective</li>
     *   <li>processingTypesPanelDirective</li>
     * </ul>
     */
    function activeTabUpdate(event) {
      event.stopPropagation();
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
