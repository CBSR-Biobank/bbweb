/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

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

/*
 * Controller for this component.
 */
/* @ngInject */
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

export default ngModule => ngModule.component('ceventGetType', component)
