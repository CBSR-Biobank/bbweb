/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function ceventGetTypeDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study: '=',
        participant: '=',
        collectionEventTypes: '='
      },
      templateUrl : '/assets/javascripts/collection/directives/ceventGetType/ceventGetType.html',
      controller: CeventGetTypeCtrl,
      controllerAs: 'vm'
    };
    return directive;
  }

    CeventGetTypeCtrl.$inject = [
    '$state',
    'CollectionEvent'
  ];

  /**
   * Used to add or edit a collection event.
   */
  function CeventGetTypeCtrl($state, CollectionEvent) {
    var vm = this;

    vm.title = 'Participant ' + vm.participant.uniqueId + ': Add collection event';
    vm.collectionEvent = new CollectionEvent();
    vm.updateCollectionEventType = updateCollectionEventType;

    // --

    function updateCollectionEventType() {
      if (vm.collectionEvent.collectionEventTypeId) {
        $state.go('home.collection.study.participant.cevents.add.details',
                  { collectionEventTypeId: vm.collectionEvent.collectionEventTypeId });
      }
    }
  }

  return ceventGetTypeDirective;
});
