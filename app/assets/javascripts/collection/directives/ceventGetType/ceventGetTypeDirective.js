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
    'gettextCatalog',
    'CollectionEvent'
  ];

  /**
   * Used to add or edit a collection event.
   */
  function CeventGetTypeCtrl($state, gettextCatalog, CollectionEvent) {
    var vm = this;

    vm.title = gettextCatalog.getString(
      'Participant {{id}}: Add collection event',
      { id: vm.participant.uniqueId });
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
