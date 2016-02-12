/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function participantViewDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study: '=',
        annotationTypes: '=',
        participant: '='
      },
      templateUrl : '/assets/javascripts/collection/directives/participantView/participantView.html',
      controller: ParticipantViewCtrl,
      controllerAs: 'vm'
    };
    return directive;
  }

  ParticipantViewCtrl.$inject = [
    '$window',
    '$state',
    '$timeout'
  ];

  /**
   *
   */
  function ParticipantViewCtrl($window, $state, $timeout) {
    var vm = this;

    vm.tabSummaryActive = false;
    vm.tabCeventsActive = false;

    init();
    activeTabUpdateFix();

    //--

    function init() {
      // initialize the panels to open state when viewing a new study
      if (vm.participant.id !== $window.localStorage.getItem('participant.panel.participantId')) {
        // this way when the user selects a new study, the panels always default to open
        $window.localStorage.setItem('participant.panel.cevents', true);

        // remember the last viewed participant
        $window.localStorage.setItem('participant.panel.participantId', vm.participant.id);
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
        vm.tabSummaryActive = ($state.current.name.startsWith('home.collection.study.participant.summary'));

        // state home.collection.study.participant.cevents has sub-states, so need to use 'statrsWith'
        vm.tabCeventsActive = ($state.current.name.startsWith('home.collection.study.participant.cevents'));
      }
    }

  }

  return participantViewDirective;
});
