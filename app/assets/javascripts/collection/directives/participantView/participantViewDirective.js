/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
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
    'gettext'
  ];

  /**
   *
   */
  function ParticipantViewCtrl($window, $state, gettext) {
    var vm = this;

    vm.tabs = [
      { heading: gettext('Summary'),    sref: 'home.collection.study.participant.summary', active: false },
      { heading: gettext('Collection'), sref: 'home.collection.study.participant.cevents', active: false }
    ];

    init();

    //--

    function init() {
      _.each(vm.tabs, function (tab, index) {
        tab.active = ($state.current.name.indexOf(tab.sref) >= 0);
        if (tab.active) {
          vm.active = index;
        }
      });

    }

  }

  return participantViewDirective;
});
