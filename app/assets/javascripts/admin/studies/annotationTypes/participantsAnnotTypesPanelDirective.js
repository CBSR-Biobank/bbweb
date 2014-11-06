define(['../../module'], function(module) {
  'use strict';

  module.directive('participantsAnnotTypesPanel', participantsAnnotTypesPanel);

  /**
   *
   */
  function participantsAnnotTypesPanel() {
    var directive = {
      require: '^tab',
      restrict: 'E',
      scope: {
        study: '=',
        annotTypes: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/annotationTypes/participantAnnotTypesPanel.html',
      controller: 'ParticipantsAnnotTypesPanelCtrl as vm'
    };
    return directive;
  }

});
