/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

var component = {
  template: require('./studyCollection.html'),
  controller: StudyCollectionController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

/*
 * Controller for this component.
 *
 * Listens to event "collection-event-type-updated" which is emmitted by a child component when
 * a Collection Event Type name is changed.
 */
/* @ngInject */
function StudyCollectionController($scope, CollectionEventType) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    // updates the selected tab in 'studyViewDirective' which is the parent directive
    $scope.$emit('tabbed-page-update', 'tab-selected');

    CollectionEventType.list(vm.study.slug).then(function (pagedResult) {
      vm.collectionEventTypes = CollectionEventType.sortByName(pagedResult.items);
    });

    $scope.$on('collection-event-type-updated', updateCollectionEventTypes);
  }

  function updateCollectionEventTypes(event, updatedCeventType) {
    var ceventTypes;
    event.stopPropagation();
    ceventTypes = _.filter(vm.collectionEventTypes, function (ceventType) {
      return ceventType.id !== updatedCeventType.id;
    });
    ceventTypes.push(updatedCeventType);
    vm.collectionEventTypes = CollectionEventType.sortByName(ceventTypes);
  }

}

export default ngModule => ngModule.component('studyCollection', component)
