/**
 * Tabs used when displaying a study.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('study.directives.studySummaryTab', []);
  mod.directive('studySummaryTab', function() {
    return {
      restrict: 'E',
      templateUrl: '/assets/templates/study/studySummaryTab.html'
    };
  });

  return mod;
});
