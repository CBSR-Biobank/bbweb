/**
 * AngularJS Component for {@link domain.participants.CollectionEvent CollectionEvents}.
 *
 * @namespace collection.components.ceventsList
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

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


/**
 * An AngularJS component that lists the {@link domain.participants.CollectionEvent CollectionEvents} for a
 * {@link domain.participants.Participant Participant}.
 *
 * @memberOf collection.components.ceventsList
 *
 * @param {domain.participants.Participant} participant - The participant to list *Collection Events*
 * for.
 *
 */
const ceventsListComponent = {
  template: require('./ceventsList.html'),
  controller: CeventsListController,
  controllerAs: 'vm',
  bindings: {
    participant: '<'
  }
};

export default ngModule => ngModule.component('ceventsList', ceventsListComponent)
