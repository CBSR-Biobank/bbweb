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
        toggleLength: '=',
        textEmptyWarning: '='
      },
      templateUrl : '/assets/javascripts/common/directives/truncateToggle.html',
      controller: controller
      //,
      //link: link
    };

    controller.$inject = ['$scope', '$filter'];

    function controller($scope, $filter) {
      if (! $scope.text) {
        $scope.text = '';
      }
      $scope.displayText      = $scope.text || '';
      $scope.toggleState      = true;
      $scope.buttonLabel      = 'Collapse';
      $scope.toggleText       = toggleText;

      console.log('***', $scope.text);

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

    // function link(scope, element, attrs) {
    //   var paragraphElement = angular.element(element.children().eq(0));
    //   var buttonElement = angular.element(element.children().eq(2));

    //   buttonElement.bind('click', toggleText);
    // }

    return directive;
  }

});
