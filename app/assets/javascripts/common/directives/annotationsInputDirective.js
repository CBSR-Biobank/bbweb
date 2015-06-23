/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  /**
   * Annotations must contain an attribute named 'annotationType' with the annotation types information.
   */
  function annotationsInputDirective() {
    return {
      restrict: 'E',
      scope: {
        annotations: '='
      },
      templateUrl : '/assets/javascripts/common/directives/annotationsInput.html',
      controller: 'AnnotationsInputCtrl as vm'
    };
  }

  AnnotationsInputCtrl.$inject = ['$scope', 'bbwebConfig'];

  /**
   *
   */
  function AnnotationsInputCtrl($scope, bbwebConfig) {
    var vm = this;

    vm.annotations = $scope.annotations;
    vm.openend = false;
    vm.format = bbwebConfig.datepickerFormat;
    vm.datePicker = {
      options: {
        startingDay: 0
      },
      open: openDatePicker
    };

    function openDatePicker($event) {
      $event.preventDefault();
      $event.stopPropagation();
      vm.opened = true;
    }
  }

  return {
    directive: annotationsInputDirective,
    controller: AnnotationsInputCtrl
  };
});
