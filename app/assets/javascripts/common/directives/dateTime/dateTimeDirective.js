/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function dateTimeDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        label:     '@',
        dateValue: '=',
        timeValue: '=',
        required:  '='
      },
      templateUrl : '/assets/javascripts/common/directives/dateTime/dateTime.html',
      controller: DateTimeCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  DateTimeCtrl.$inject = ['bbwebConfig'];

  function DateTimeCtrl(bbwebConfig) {
    var vm = this;

    vm.opened = false;
    vm.options = {
      formatYear:  bbwebConfig.datepickerFormat.year,
      formatMonth: bbwebConfig.datepickerFormat.month,
      formatDay:   bbwebConfig.datepickerFormat.day,
      minDate:     '2000-01-01',
      showWeeks:   false,
      startingDay: 0
    };

    vm.open = openDatePicker;

    function openDatePicker($event) {
      $event.preventDefault();
      $event.stopPropagation();
      vm.opened = true;
    }

  }

  return dateTimeDirective;
});
