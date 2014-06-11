/**
 * Tabs used when displaying a study.
 */
define(['angular'], function(angular) {
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
      templateUrl: '/assets/templates/study/studyParticipantsTab.html'
    };
  });

  return mod;
});
