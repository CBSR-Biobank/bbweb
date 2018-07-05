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
class CeventsAddAndSelectController {

  constructor($state,
              BbwebError,
              CollectionEvent,
              CollectionEventTypeName) {
    'ngInject';
    Object.assign(this,
                  {
                    $state,
                    BbwebError,
                    CollectionEvent,
                    CollectionEventTypeName
                  });
  }

  $onInit() {
    this.pagerOptions = {
      page:      1,
      limit:  5,
      sortField: 'visitNumber'
    };

    this.CollectionEvents = [];
    this.visitNumberFilter = '';
    this.collectionEventRefresh();
  }

  /*
   * Parent component can trigger a collection event reload by calling updating this binding.
   */
  $onChanges(changed) {
    if (changed.collectionEventsRefresh) {
      this.collectionEventRefresh();
    }
  }

  getDisplayState() {
    if (this.pagedResult.total > 0) {
      return DisplayStates.HAVE_RESULTS;
    }
    return (this.visitNumberFilter === '') ? DisplayStates.NONE_ADDED : DisplayStates.NO_RESULTS;
  }

  isDisplayStateNoneAdded() {
    return (this.displayState === DisplayStates.NONE_ADDED);
  }

  isDisplayStateNoResults() {
    return (this.displayState === DisplayStates.NO_RESULTS);
  }

  isDisplayStateHaveResults() {
    return (this.displayState === DisplayStates.HAVE_RESULTS);
  }

  getShowPagination() {
    return (this.displayState === DisplayStates.HAVE_RESULTS) &&
      (this.pagedResult.maxPages > 1);
  }

  collectionEventRefresh() {
    this.CollectionEvent.list(this.participant.id, this.pagerOptions)
      .then(pagedResult => {
        this.collectionEvents = pagedResult.items;
        this.pagedResult = pagedResult;
        this.displayState = this.getDisplayState();
        this.showPagination = this.getShowPagination();
        this.paginationNumPages = pagedResult.maxPages;
      });
  }

  pageChanged() {
    this.collectionEventRefresh();
    this.$state.go('home.collection.study.participant.cevents');
  }

  add() {
    this.CollectionEventTypeName.list(this.participant.studyId)
      .then(typeNames => {
        if (typeNames.length > 1) {
          this.$state.go('home.collection.study.participant.cevents.add');
        } else {
          this.$state.go('home.collection.study.participant.cevents.add.details',
                    { eventTypeSlug: typeNames[0].slug });
        }
      })
  }

  eventInformation(cevent) {
    this.$state.go('home.collection.study.participant.cevents.details',
                   { visitNumber: cevent.visitNumber });
  }

  visitFilterUpdated() {
    if (this.visitNumberFilter) {
      this.pagerOptions.filter = 'visitNumber::' + this.visitNumberFilter;
    } else {
      this.pagerOptions.filter = '';
    }
    this.pagerOptions.page = 1;
    this.collectionEventRefresh();
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
