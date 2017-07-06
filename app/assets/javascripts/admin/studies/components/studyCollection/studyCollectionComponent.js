/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl: '/assets/javascripts/admin/studies/components/studyCollection/studyCollection.html',
    controller: StudyCollectionController,
    controllerAs: 'vm',
    bindings: {
        study: '<'
    }
  };

  StudyCollectionController.$inject = ['$scope', 'CollectionEventType'];

  /*
   * Controller for this component.
   *
   * Listens to event "collection-event-type-updated" which is emmitted by a child component when
   * a Collection Event Type name is changed.
   */
  function StudyCollectionController($scope, CollectionEventType) {
    var vm = this;

    vm.$onInit = onInit();

    //--

    function onInit() {
      // updates the selected tab in 'studyViewDirective' which is the parent directive
      $scope.$emit('tabbed-page-update', 'tab-selected');

      CollectionEventType.list(vm.study.id).then(function (pagedResult) {
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

  return component;
});
