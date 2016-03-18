/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  /**
   *
   */
  function ceventsAddAndSelectDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        participant: '=',
        collectionEventsPagedResult: '=',
        collectionEventTypes: '='
      },
      transclude: true,
      templateUrl : '/assets/javascripts/collection/directives/ceventsAddAndSelect/ceventsAddAndSelect.html',
      controller: CeventsAddAndSelectCtrl,
      controllerAs: 'vm'
    };
    return directive;
  }

  CeventsAddAndSelectCtrl.$inject = [
    '$state',
    'CollectionEvent'
  ];

  /**
   *
   */
  function CeventsAddAndSelectCtrl($state,
                                   CollectionEvent) {
    var vm = this;

    if (vm.collectionEventTypes.length <= 0) {
      throw new Error('no collection event types defined for this study');
    }

    vm.displayStates = {
      NO_RESULTS: 0,
      HAVE_RESULTS: 1
    };

    vm.collectionEvents = setCollectionEventType(vm.collectionEventsPagedResult.items);
    vm.displayState     = getDisplayState();
    vm.showPagination   = getShowPagination();

    vm.pagerOptions = {
      page:      1,
      pageSize:  5,
      sortField: 'visitNumber'
    };

    vm.pageChanged          = pageChanged;
    vm.add                  = add;
    vm.eventInformation     = eventInformation;

    // --

    function setCollectionEventType(cevents) {
      _.map(cevents, function (cevent) {
        var ceventType = _.findWhere(vm.collectionEventTypes, { id: cevent.collectionEventTypeId });
        if (ceventType) {
          cevent.setCollectionEventType(ceventType);
        }
      });
      return cevents;
    }

    function getDisplayState() {
      return (vm.collectionEventsPagedResult.total > 0) ?
        vm.displayStates.HAVE_RESULTS : vm.displayStates.NO_RESULTS;
    }

    function getShowPagination() {
      return (vm.displayState === vm.displayStates.HAVE_RESULTS) &&
        (vm.collectionEventsPagedResult.maxPages > 1);
    }

    function updateCollectionEvents() {
      CollectionEvent.list(vm.participant.id, vm.pagerOptions).then(function (pagedResult) {
        vm.collectionEventsPagedResult = pagedResult;
        vm.collectionEvents = setCollectionEventType(pagedResult.items);
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
      if (vm.collectionEventTypes.length > 1) {
        $state.go('home.collection.study.participant.cevents.add');
      } else {
        $state.go('home.collection.study.participant.cevents.add.details',
                  { collectionEventTypeId: vm.collectionEventTypes[0].id });
      }
    }

    function eventInformation(cevent) {
      $state.go('home.collection.study.participant.cevents.details',
                { collectionEventId: cevent.id });
    }
  }

  return ceventsAddAndSelectDirective;
});
