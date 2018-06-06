/**
 * AngularJS Component for used in {@link domain.studies.CollectionEventType CollectionEventType}
 * administration.
 *
 * @namespace admin.studies.components.ceventTypesAddAndSelect
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
/* @ngInject */
function CeventTypesAddAndSelectController($scope,
                                           $state,
                                           gettextCatalog,
                                           CollectionEventType) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
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

    vm.nameFilter        = '';
    vm.displayState      = vm.displayStates.NOT_CONFIGURED;
    vm.add               = add;
    vm.select            = select;
    vm.getRecurringLabel = getRecurringLabel;
    vm.pageChanged       = pageChanged;
    vm.nameFilterUpdated = nameFilterUpdated;

    updateCollectionEvents();
  }

  function updateCollectionEvents() {
    CollectionEventType.list(vm.study.slug, vm.pagerOptions).then(function (pagedResult) {
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
    $state.go('home.admin.studies.study.collection.ceventType',
              { ceventTypeSlug: ceventType.slug });
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

/**
 * An AngularJS component that displays all the {@link domain.studies.CollectionEventType
 * CollectionEventTypes} for a {@link domain.studies.Study Study} and allows the user to select one.
 *
 * @memberOf admin.studies.components.ceventTypesAddAndSelect
 *
 * @param {domain.studies.Study} study - the study the collection event types belongs to.
 *
 * @param {Array<domain.studies.CollectionEventType>} collectionEventTypes - the collection event types the
 * study has.
 */
const ceventTypesAddAndSelectComponent = {
  template: require('./ceventTypesAddAndSelect.html'),
  controller: CeventTypesAddAndSelectController,
  controllerAs: 'vm',
  bindings: {
    study:                '<',
    collectionEventTypes: '<',
    addAllowed:           '<'
  }
}

export default ngModule => ngModule.component('ceventTypesAddAndSelect', ceventTypesAddAndSelectComponent)
