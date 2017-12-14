/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: require('./ceventsAddAndSelect.html'),
  controller: CeventsAddAndSelectDirective,
  controllerAs: 'vm',
  bindings: {
    participant:            '<',
    updateCollectionEvents: '<'
  }
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
    vm.displayStates = {
      NO_RESULTS: 0,
      HAVE_RESULTS: 1,
      NONE_ADDED: 2
    };

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
    vm.collectionEventError = false;

    updateCollectionEvents();
  }

  /*
   * Parent component can trigger a collection event reload by calling updating this binding.
   */
  function onChanges() {
    if (vm.updateCollectionEvents) {
      updateCollectionEvents();
    }
  }

  function getDisplayState() {
    if (vm.pagedResult.total > 0) {
      return vm.displayStates.HAVE_RESULTS;
    }
    return (vm.visitNumberFilter === '') ? vm.displayStates.NONE_ADDED : vm.displayStates.NO_RESULTS;
  }

  function getShowPagination() {
    return (vm.displayState === vm.displayStates.HAVE_RESULTS) &&
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
          $state.go('home.collection.study.participant.cevents.add.details',
                    { eventTypeId: vm.collectionEventTypes[0].id });
        }
      })
  }

  function eventInformation(cevent) {
    $state.go('home.collection.study.participant.cevents.details', {
      eventTypeId: cevent.collectionEventTypeId,
      eventId:     cevent.id
    });
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
