/**
 * AngularJS Component for {@link domain.participants.CollectionEvent CollectionEvents}.
 *
 * @namespace collection.components.ceventsAddAndSelect
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

const DisplayStates = {
  NO_RESULTS: 0,
  HAVE_RESULTS: 1,
  NONE_ADDED: 2
};

/*
 * Controller for this component.
 */
/* @ngInject */
function CeventsAddAndSelectController($state,
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

    collectionEventRefresh();
  }

  /*
   * Parent component can trigger a collection event reload by calling updating this binding.
   */
  function onChanges(changed) {
    if (changed.collectionEventsRefresh) {
      collectionEventRefresh();
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

  function collectionEventRefresh() {
    CollectionEvent.list(vm.participant.id, vm.pagerOptions).then(function (pagedResult) {
      vm.collectionEvents = pagedResult.items;
      vm.pagedResult = pagedResult;
      vm.displayState = getDisplayState();
      vm.showPagination = getShowPagination();
      vm.paginationNumPages = pagedResult.maxPages;
    });
  }

  function pageChanged() {
    collectionEventRefresh();
    $state.go('home.collection.study.participant.cevents');
  }

  function add() {
    CollectionEventTypeName.list(vm.participant.studyId)
      .then(typeNames => {
        if (typeNames.length > 1) {
          $state.go('home.collection.study.participant.cevents.add');
        } else {
          $state.go('home.collection.study.participant.cevents.add.details',
                    { eventTypeSlug: typeNames[0].slug });
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
    collectionEventRefresh();
  }
}

/**
 * An AngularJS component that displays all the {@link domain.participants.CollectionEvent CollectionEvents}
 * for a {@link domain.participants.Participant Participant} and allows the user to select one.
 *
 * @namespace collection.components.ceventsAddAndSelect
 *
 * @param {domain.participants.Participant} participant - The participant to select a *Collection Event*
 * from.
 *
 * @param {int} collectionEventsRefresh - a parent component should increment this value when it wishes to
 * have this component refresh the list of *Collection Events*.
 */
const ceventsAddAndSelectComponent = {
  template: require('./ceventsAddAndSelect.html'),
  controller: CeventsAddAndSelectController,
  controllerAs: 'vm',
  bindings: {
    participant:             '<',
    collectionEventsRefresh: '<'
  }
};

export default ngModule => ngModule.component('ceventsAddAndSelect', ceventsAddAndSelectComponent)
