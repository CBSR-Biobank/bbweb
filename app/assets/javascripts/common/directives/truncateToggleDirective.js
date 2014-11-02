define(['../module'], function(module) {
  'use strict';

  module.directive('truncateToggle', truncateToggle);

  /**
   *
   */
  function truncateToggle() {
    var directive = {
      restrict: 'E',
      replace: 'true',
      scope: {
        text: '=',
        toggleLength: '='
      },
      template : '<div><p>{{displayText}}</p>' +
        '<button class="btn btn-xs btn-primary" ng-click="toggleText()">{{buttonLabel}}</button></div>',
      controller: controller
    };

    controller.$inject = ['$scope', '$filter'];

    function controller($scope, $filter) {
      $scope.displayText = $scope.text;
      $scope.toggleState = true;
      $scope.buttonLabel = 'Collapse';
      $scope.toggleText = toggleText;

      //---

      function toggleText() {
        if ($scope.toggleState) {
          $scope.displayText = $filter('truncate')($scope.text, $scope.toggleLength);
          $scope.buttonLabel = 'Expand';
        } else {
          $scope.displayText = $scope.text;
          $scope.buttonLabel = 'Collapse';
        }
        $scope.toggleState = !$scope.toggleState;
      }
    }

    return directive;
  }

});
