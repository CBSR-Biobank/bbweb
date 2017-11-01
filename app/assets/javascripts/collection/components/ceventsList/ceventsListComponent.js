/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/*
 * This component lists the collection events for a participant.
 */
var component = {
  template: require('./ceventsList.html'),
  controller: CeventsListController,
  controllerAs: 'vm',
  bindings: {
    participant: '<'
  }
};

/*
 * Controller for this component.
 */
/* @ngInject */
function CeventsListController($q, $scope, CollectionEventType) {
  var vm = this;
  vm.$onInit = onInit;
  vm.collectionEventTypes = [];

  //---

  function onInit() {
    vm.updateCollectionEvents = 0;

    $scope.$on('collection-event-updated', updateCollectionEvents);

    CollectionEventType.list(vm.participant.studyId)
      .then(pagedResults => {
        vm.collectionEventTypes = pagedResults.items;
        if (vm.collectionEventTypes.length <= 0) {
          return $q.reject(new Error('no collection event types defined for this study'));
        }
        return $q.when(pagedResults.items);
      });
  }

  function updateCollectionEvents(event) {
    event.stopPropagation();
    vm.updateCollectionEvents += 1;
  }
}


export default ngModule => ngModule.component('ceventsList', component)
