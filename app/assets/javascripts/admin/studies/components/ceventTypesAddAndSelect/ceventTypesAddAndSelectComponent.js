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
      study:                '<',
      collectionEventTypes: '<'
    }
  };

  CeventTypesAddAndSelectController.$inject = [
    '$scope',
    '$state',
    'gettextCatalog',
    'CollectionEventType'
  ];

  /*
   * Controller for this component.
   */
  function CeventTypesAddAndSelectController($scope,
                                             $state,
                                             gettextCatalog,
                                             CollectionEventType) {
    var vm = this;

    vm.displayStates = {
      NO_RESULTS: 0,
      HAVE_RESULTS: 1,
      NOT_CONFIGURED: 2
    };

    vm.pagerOptions = {
      page:   1,
      limit:  5,
      sortField: 'name'
    };

    vm.$onInit           = onInit;
    vm.nameFilter        = '';
    vm.displayState      = vm.displayStates.NOT_CONFIGURED;
    vm.add               = add;
    vm.select            = select;
    vm.getRecurringLabel = getRecurringLabel;
    vm.pageChanged       = pageChanged;
    vm.nameFilterUpdated = nameFilterUpdated;

    //--

    function onInit() {
      updateCollectionEvents();
    }

    function updateCollectionEvents() {
      CollectionEventType.list(vm.study.id, vm.pagerOptions).then(function (pagedResult) {
        vm.pagedResult          = pagedResult;
        vm.collectionEventTypes = CollectionEventType.sortByName(pagedResult.items);
        vm.displayState         = getDisplayState();
        vm.showPagination       = getShowPagination();
        vm.paginationNumPages   = pagedResult.maxPages;
      });
    }

    function getDisplayState() {
      if (vm.pagedResult.total > 0) {
        return vm.displayStates.HAVE_RESULTS;
      }
      return (vm.nameFilter === '') ? vm.displayStates.NOT_CONFIGURED: vm.displayStates.NO_RESULTS;
    }

    function getShowPagination() {
      return (vm.displayState === vm.displayStates.HAVE_RESULTS) && (vm.pagedResult.maxPages > 1);
    }

    function pageChanged() {
      updateCollectionEvents();
      $state.go('home.admin.studies.study.collection');
    }

    function add() {
      $state.go('home.admin.studies.study.collection.ceventTypeAdd');
    }

    function select(ceventType) {
      $state.go('home.admin.studies.study.collection.ceventType', { ceventTypeId: ceventType.id });
    }

    function getRecurringLabel(ceventType) {
      return ceventType.recurring ?
        gettextCatalog.getString('Rec') : gettextCatalog.getString('NonRec');
    }

    function nameFilterUpdated() {
      if (vm.nameFilter) {
        vm.pagerOptions.filter = 'name:like:' + vm.nameFilter;
      } else {
        vm.pagerOptions.filter = '';
      }
      vm.pagerOptions.page = 1;
      pageChanged();
    }
  }

  return component;
});