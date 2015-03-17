define([], function(){
  'use strict';

  /**
   * Annotations must contain an attribute named 'annotationType' with the annotation types information.
   */
  function annotationsInputDirective() {
    return {
      restrict: 'E',
      scope: {
        annotationHelpers: '='
      },
      templateUrl : '/assets/javascripts/common/directives/annotationsInput.html',
      controller: 'AnnotationsInputCtrl as vm'
    };
  }

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

  return {
    directive: annotationsInputDirective,
    controller: AnnotationsInputCtrl
  };
});
