/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function truncateToggleDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        text:             '=',
        toggleLength:     '=',
        textEmptyWarning: '@',
        allowEdit:        '=',
        onEdit:           '&'
      },
      templateUrl : '/assets/javascripts/common/directives/truncateToggle/truncateToggle.html',
      controller: TruncateToggleCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  TruncateToggleCtrl.$inject = [
    '$scope',
    '$filter',
    'gettextCatalog'
  ];

  function TruncateToggleCtrl($scope, $filter, gettextCatalog) {
    var vm = this,
        showLessLabel = gettextCatalog.getString('Show less'),
        showMoreLabel = gettextCatalog.getString('Show more');

    vm.displayText = vm.text || '';
    vm.toggleState = true;
    vm.buttonLabel = showLessLabel;
    vm.toggleText  = toggleText;

    $scope.$watch('vm.text', function() {
      vm.displayText = vm.text || '';
    });

    //---

    function toggleText() {
      if (vm.toggleState) {
        vm.displayText = $filter('truncate')(vm.text, vm.toggleLength);
        vm.buttonLabel = showMoreLabel;
      } else {
        vm.displayText = vm.text;
        vm.buttonLabel = showLessLabel;
      }
      vm.toggleState = !vm.toggleState;
    }

  }

  return truncateToggleDirective;

});
