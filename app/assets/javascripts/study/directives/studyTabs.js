/**
 * Tabs used when displaying a study.
 *
 *  FIXME: See https://docs.angularjs.org/guide/directive, section "Creating Directives that Communicate" to
 * improve this code.
 */
define(['angular', '../controllers'], function(angular, controllers) {
  'use strict';

  var mod = angular.module('study.directives.studyTabs', ['study.services'])
    .directive('studyTabs', function() {
      return {
        restrict: 'E',
        transclude: true,
        templateUrl: '/assets/javascripts/study/studyTabs.html',
        controller: function($scope) {
          var panes = $scope.panes = [];

          $scope.select = function(pane) {
            angular.forEach(panes, function(pane) {
              pane.selected = false;
            });
            pane.selected = true;
          };

          this.addPane = function(pane) {
            if (panes.length === 0) {
              $scope.select(pane);
            }
            panes.push(pane);
          };
        }
      };
    })

    .directive('summaryPane',function() {
      return {
        require: '^studyTabs',
        restrict: 'E',
        transclude: true,
        scope: {
          title: '@'
        },
        templateUrl: '/assets/javascripts/study/studyPane.html',
        link: function(scope, element, attrs, tabsCtrl) {
          tabsCtrl.addPane(scope);
        }
      };
    })

    .directive('participantsPane', function() {
      /**
       * Displays study annotation type summary information in a table. The user can then select an
       * annotation to display more informaiton.
       */
      return {
        require: '^studyTabs',
        restrict: 'E',
        transclude: true,
        scope: {
          title: '@'
        },
        templateUrl: '/assets/javascripts/study/studyPane.html',
        controller: controllers.AnnotationTypeDirectiveCtrl,
        link: function(scope, element, attrs, tabsCtrl) {
          tabsCtrl.addPane(scope);
        }
      };
    });

  return mod;
});
