/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/admin/studies/components/ceventTypesAddAndSelect/ceventTypesAddAndSelect.html',
    controller: CeventTypesAddAndSelectController,
    controllerAs: 'vm',
    bindings: {
      collectionEventTypes: '='
    }
  };

  CeventTypesAddAndSelectController.$inject = [
    '$scope',
    '$state',
    'gettextCatalog'
  ];

  /*
   * Controller for this component.
   */
  function CeventTypesAddAndSelectController($scope,
                                             $state,
                                             gettextCatalog) {
    var vm = this;

    vm.showPagination    = false;
    vm.add               = add;
    vm.select            = select;
    vm.getRecurringLabel = getRecurringLabel;

    //--

    function add() {
      $state.go('home.admin.studies.study.collection.ceventTypeAdd');
    }

    function select(ceventType) {
      $state.go('home.admin.studies.study.collection.ceventType', { ceventTypeId: ceventType.id });
    }

    function getRecurringLabel(ceventType) {
      return ceventType.recurring ?
        gettextCatalog.getString('Recurring') : gettextCatalog.getString('Not recurring');
    }
  }

  return component;
});
