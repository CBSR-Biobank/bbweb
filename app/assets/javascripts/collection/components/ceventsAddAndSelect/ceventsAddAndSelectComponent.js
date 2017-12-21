/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

const component = {
  template: require('./ceventsAddAndSelect.html'),
  controller: CeventsAddAndSelectDirective,
  controllerAs: 'vm',
  bindings: {
    participant:            '<',
    updateCollectionEvents: '<'
  }
};

const DisplayStates = {
  NO_RESULTS: 0,
  HAVE_RESULTS: 1,
  NONE_ADDED: 2
};

/*
 * Controller for this component.
 */
/* @ngInject */
function CeventsAddAndSelectDirective($state,
                                      BbwebError,
                                      CollectionEvent,
                                      CollectionEventTypeName) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {

    vm.pagerOptions = {
      page:      1,
      limit:  5,
      sortField: 'visitNumber'
    };

    vm.$onChanges           = onChanges;
    vm.visitNumberFilter    = '';
    vm.pageChanged          = pageChanged;
    vm.add                  = add;
    vm.eventInformation     = eventInformation;
    vm.visitFilterUpdated   = visitFilterUpdated;

    updateCollectionEvents();
  }

  /*
   * Parent component can trigger a collection event reload by calling updating this binding.
   */
  function onChanges(changed) {
    if (changed.updateCollectionEvents) {
      updateCollectionEvents();
    }
  }

  function getDisplayState() {
    if (vm.pagedResult.total > 0) {
      return DisplayStates.HAVE_RESULTS;
    }
    return (vm.visitNumberFilter === '') ? DisplayStates.NONE_ADDED : DisplayStates.NO_RESULTS;
  }

  function getShowPagination() {
    return (vm.displayState === DisplayStates.HAVE_RESULTS) &&
      (vm.pagedResult.maxPages > 1);
  }

  function updateCollectionEvents() {
    CollectionEvent.list(vm.participant.id, vm.pagerOptions).then(function (pagedResult) {
      vm.collectionEvents = pagedResult.items;
      vm.pagedResult = pagedResult;
      vm.displayState = getDisplayState();
      vm.showPagination = getShowPagination();
      vm.paginationNumPages = pagedResult.maxPages;
    });
  }

  function pageChanged() {
    updateCollectionEvents();
    $state.go('home.collection.study.participant.cevents');
  }

  function add() {
    CollectionEventTypeName.list(vm.participant.studyId)
      .then(typeNames => {
        if (typeNames.length > 1) {
          $state.go('home.collection.study.participant.cevents.add');
        } else {
          $state.go('home.collection.study.participant.cevents.add.details', { eventTypeId: typeNames[0].id });
        }
      })
  }

  function eventInformation(cevent) {
    $state.go('home.collection.study.participant.cevents.details', { visitNumber: cevent.visitNumber });
  }

  function visitFilterUpdated() {
    if (vm.visitNumberFilter) {
      vm.pagerOptions.filter = 'visitNumber::' + vm.visitNumberFilter;
    } else {
      vm.pagerOptions.filter = '';
    }
    vm.pagerOptions.page = 1;
    updateCollectionEvents();
  }
}

export default ngModule => ngModule.component('ceventsAddAndSelect', component)
