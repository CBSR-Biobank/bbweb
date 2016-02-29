/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Description
   */
  function ceventTypesAddAndSelectDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study:                  '='
      },
      templateUrl : '/assets/javascripts/admin/studies/ceventTypes/directives/ceventTypesAddAndSelect/ceventTypesAddAndSelect.html',
      controller: CeventTypesAddAndSelectCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CeventTypesAddAndSelectCtrl.$injector = [ '$state' ];

  function CeventTypesAddAndSelectCtrl($state) {
    var vm = this;

    vm.showPagination = false;
    vm.add = add;
    vm.information = information;
    vm.getRecurringLabel = getRecurringLabel;

    // TODO: add pagination

    //-

    function add() {
      $state.go('home.admin.studies.study.collection.ceventTypeAdd');
    }

    function information(ceventType) {
      $state.go('home.admin.studies.study.collection.view', { ceventTypeId: ceventType.id });
    }

    function getRecurringLabel(ceventType) {
      return ceventType.recurring ? 'Recurring' : 'Not recurring';
    }
  }

  return ceventTypesAddAndSelectDirective;

});
