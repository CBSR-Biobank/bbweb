/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    template: require('./ceventGetType.html'),
    controller: CeventGetTypeController,
    controllerAs: 'vm',
    bindings: {
      study:                '<',
      participant:          '<',
      collectionEventTypes: '<'
    }
  };

  CeventGetTypeController.$inject = [
    '$state',
    'CollectionEvent',
    'gettextCatalog'
  ];

  /*
   * Controller for this component.
   */
  function CeventGetTypeController($state, CollectionEvent, gettextCatalog) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.title = gettextCatalog.getString('Participant {{id}}: Add collection event',
                                          { id: vm.participant.uniqueId });
      vm.collectionEvent = new CollectionEvent();
      vm.updateCollectionEventType = updateCollectionEventType;
    }

    function updateCollectionEventType() {
      if (vm.collectionEvent.collectionEventTypeId) {
        $state.go('home.collection.study.participant.cevents.add.details',
                  { collectionEventTypeId: vm.collectionEvent.collectionEventTypeId });
      }
    }
  }

  return component;
});
