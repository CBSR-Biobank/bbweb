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
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        annotations: '='
      },
      templateUrl : '/assets/javascripts/common/directives/annotationsInput/annotationsInput.html',
      controller: AnnotationsInputCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  AnnotationsInputCtrl.$inject = ['bbwebConfig'];

  /**
   *
   */
  function AnnotationsInputCtrl(bbwebConfig) {
    var vm = this;

    vm.opened = false;
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

  return annotationsInputDirective;
});
