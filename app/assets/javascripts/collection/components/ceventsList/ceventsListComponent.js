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
function CeventsListController($q, $scope) {
  var vm = this;
  vm.$onInit = onInit;
  vm.updateCollectionEvents = updateCollectionEvents;

  //---

  function onInit() {
    vm.updateCollectionEvents = 0;

    $scope.$on('collection-event-updated', updateCollectionEvents);
  }

  function updateCollectionEvents(event) {
    event.stopPropagation();
    vm.updateCollectionEvents += 1;
  }
}


export default ngModule => ngModule.component('ceventsList', component)
