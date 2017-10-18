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
    participant:          '<',
    collectionEventTypes: '<'
  }
};

CeventsListController.$inject = ['$scope'];

/*
 * Controller for this component.
 */
function CeventsListController($scope) {
  var vm = this;
  vm.$onInit = onInit;

  //---

  function onInit() {
    vm.updateCollectionEvents = 0;
    if (vm.collectionEventTypes.length <= 0) {
      throw new Error('no collection event types defined for this study');
    }

    $scope.$on('collection-event-updated', updateCollectionEvents);
  }

  function updateCollectionEvents(event) {
    event.stopPropagation();
    vm.updateCollectionEvents += 1;
  }
}


export default ngModule => ngModule.component('ceventsList', component)
