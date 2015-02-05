define(['../module'], function(module) {
  'use strict';

  module.directive('annotationsInput', annotationsInputDirective);

  /**
   * Annotations must contain an attribute named 'annotationType' with the annotation types information.
   */
  function annotationsInputDirective() {
    var directive = {
      restrict: 'E',
      scope: {
        annotationHelpers: '='
      },
      templateUrl : '/assets/javascripts/common/directives/annotationsInput.html',
      controller: 'AnnotationsInputCtrl as vm'
    };
    return directive;
  }

  module.controller('AnnotationsInputCtrl', AnnotationsInputCtrl);
  AnnotationsInputCtrl.$inject = ['$scope'];

  /**
   *
   */
  function AnnotationsInputCtrl($scope) {
    var vm = this;

    vm.annotationHelpers = $scope.annotationHelpers;
    vm.datePicker = {
      opened: false,
      open: openDatePicker,
      options: {
        formatYear: 'yyyy'
      }
    };

    function openDatePicker($event) {
      $event.preventDefault();
      $event.stopPropagation();

      vm.datePicker.opened = true;
    }

  }

});
