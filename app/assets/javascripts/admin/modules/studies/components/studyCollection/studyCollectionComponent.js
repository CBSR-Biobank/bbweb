/**
 * AngularJS Component for {@link domain.studies.Study Study} administration.
 *
 * @namespace admin.studies.components.studyCollection
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

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

/**
 * An AngularJS component that displays the collection aspect of the configuration for a {@link
 * domain.studies.Study Study}.
 *
 * The user can add, modify, or remove a {@link domain.studies.CollectionEventType CollectionEventTypes}.
 *
 * @memberOf admin.studies.components.studyCollection
 *
 * @param {domain.studies.Study} study - the *Study* the *Collection Event Types* belong to.
 */
const studyCollectionComponent = {
  template: require('./studyCollection.html'),
  controller: StudyCollectionController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

export default ngModule => ngModule.component('studyCollection', studyCollectionComponent)
