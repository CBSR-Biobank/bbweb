/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
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

  /*
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
      HAVE_RESULTS: 1,
      NONE_ADDED: 2
    };

    vm.pagerOptions = {
      page:      1,
      limit:  5,
      sortField: 'visitNumber'
    };

    vm.visitNumberFilter = '';
    vm.pageChanged          = pageChanged;
    vm.add                  = add;
    vm.eventInformation     = eventInformation;
    vm.visitFilterUpdated   = visitFilterUpdated;
    vm.collectionEventError = false;

    init();

    // --

    function init() {
      updateCollectionEvents();
    }

    function setCollectionEventType(cevents) {
      _.map(cevents, function (cevent) {
        var ceventType = _.find(vm.collectionEventTypes, { id: cevent.collectionEventTypeId });
        if (_.isUndefined(ceventType)) {
          vm.collectionEventError = true;
        } else {
          cevent.setCollectionEventType(ceventType);
        }
      });
      return cevents;
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
        vm.pagedResult = pagedResult;
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

  return ceventsAddAndSelectDirective;
});
