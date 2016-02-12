/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function participantSummaryDirective() {
    var directive = {
      restrict: 'EA',
      scope: {},
      bindToController: {
        study: '=',
        participant: '='
      },
      templateUrl : '/assets/javascripts/collection/directives/participantSummary/participantSummary.html',
      controller: ParticipantSummaryCtrl,
      controllerAs: 'vm'
    };
    return directive;

  }

  ParticipantSummaryCtrl.$inject = [
    '$state'
  ];

  // TODO: add update buttons for each setting

  /**
   *
   */
  function ParticipantSummaryCtrl($state) {

  }

  return participantSummaryDirective;
});
