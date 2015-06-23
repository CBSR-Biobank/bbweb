/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function(){
  'use strict';

  /**
   *
   */
  function truncateToggleDirectiveFactory() {
    var directive = {
      restrict: 'E',
      replace: true,
      scope: {
        text: '=',
        toggleLength: '=',
        textEmptyWarning: '@'
      },
      templateUrl : '/assets/javascripts/common/directives/truncateToggle.html',
      controller: controller
    };

    controller.$inject = ['$scope', '$filter'];

    function controller($scope, $filter) {
      $scope.displayText = $scope.text || '';
      $scope.toggleState = true;
      $scope.buttonLabel = 'Collapse';
      $scope.toggleText  = toggleText;

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

  return truncateToggleDirectiveFactory;
});
