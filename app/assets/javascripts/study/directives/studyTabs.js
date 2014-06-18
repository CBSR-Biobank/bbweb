/**
 * Tabs used when displaying a study.
 */
define(['angular', '../controllers'], function(angular, controllers) {
  'use strict';

  var mod = angular.module('study.directives.studyTabs', []);
  mod.directive('studySummaryTab', function() {
    return {
      restrict: 'E',
      templateUrl: '/assets/templates/study/studySummaryTab.html'
    };
  });

  mod.directive('studyParticipantsTab', function() {
    return {
      restrict: 'E',
      scope: {},
      templateUrl: '/assets/templates/study/studyParticipantsTab.html',
      controller: controllers.AnnotationTypeDirectiveCtrl
    };
  });

  return mod;
});
