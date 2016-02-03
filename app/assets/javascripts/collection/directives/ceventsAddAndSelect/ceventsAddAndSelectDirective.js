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
        collectionEvents: '=',
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
    '$state'
  ];

  /**
   *
   */
  function CeventsAddAndSelectCtrl($state) {
    var vm = this;

    if (vm.collectionEventTypes.length <= 0) {
      throw new Error('no collection event types defined for this study');
    }

    vm.add                  = add;
    vm.eventInformation     = eventInformation;

    _.each(vm.collectionEvents, function (cevent) {
      var ceventType = _.findWhere(vm.collectionEventTypes, { id: cevent.collectionEventTypeId });
      if (ceventType) {
        cevent.setCollectionEventType(ceventType);
      }
    });


    // --

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
