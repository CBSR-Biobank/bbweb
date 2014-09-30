/**
 * Tabs used when displaying a study.
 *
 * FIXME: See https://docs.angularjs.org/guide/directive, section 'Creating Directives that Communicate' to
 * improve this code.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.directives.studyPanels', ['studies.services']);

  mod.directive('participantsAnnotTypesPanel', function() {
    return {
      require: '^tab',
      restrict: 'E',
      templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotTypesPanel.html'
    };
  });

  mod.directive('ceventAnnotTypesPanel', function() {
    return {
      require: '^tab',
      restrict: 'E',
      templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotTypesPanel.html'
    };
  });

  mod.directive('spcLinkAnnotTypesPanel', function() {
    return {
      require: '^tab',
      restrict: 'E',
      templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotTypesPanel.html'
    };
  });

  mod.directive('specimenGroupsPanel', function() {
    return {
      require: '^tab',
      restrict: 'E',
      templateUrl: '/assets/javascripts/admin/studies/specimenGroups/specimenGroupsPanel.html'
    };
  });

  mod.directive('ceventTypesPanel', function() {
    return {
      require: '^tab',
      restrict: 'E',
      templateUrl: '/assets/javascripts/admin/studies/ceventTypes/ceventTypesPanel.html'
    };
  });

  mod.directive('processingTypesPanel', function() {
    return {
      require: '^tab',
      restrict: 'E',
      templateUrl: '/assets/javascripts/admin/studies/processing/processingTypePanel.html'
    };
  });

  mod.directive('spcLinkTypesPanel', function() {
    return {
      require: '^tab',
      restrict: 'E',
      templateUrl: '/assets/javascripts/admin/studies/processing/spcLinkTypesPanel.html'
    };
  });

  return mod;
});
