/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function(require) {
  'use strict';

  var _ = require('lodash');

  /**
   *
   * Listens to event "collection-event-type-name-changed" which is emmitted by a child component when
   * a Collection Event Type name is changed.
   */
  function studyCollectionDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/directives/studyCollection/studyCollection.html',
      controller: StudyCollectionCtrl,
      controllerAs: 'vm'
    };
    return directive;
  }

  StudyCollectionCtrl.$inject = ['$scope', 'CollectionEventType'];

  function StudyCollectionCtrl($scope, CollectionEventType) {
    var vm = this;

    init();

    //--

    function init() {
      // updates the selected tab in 'studyViewDirective' which is the parent directive
      $scope.$emit('tabbed-page-update', 'tab-selected');

      CollectionEventType.list(vm.study.id).then(function (list) {
        vm.collectionEventTypes = sortCollectionEvents(list);
      });

      $scope.$on('collection-event-type-name-changed', updateCollectionEventTypes);
    }

    function updateCollectionEventTypes(event, updatedCeventType) {
      var ceventTypes;
      event.stopPropagation();
      ceventTypes = _.filter(vm.collectionEventTypes, function (ceventType) {
        return ceventType.id !== updatedCeventType.id;
      });
      ceventTypes.push(updatedCeventType);
      vm.collectionEventTypes = sortCollectionEvents(ceventTypes);
    }

    function sortCollectionEvents(ceventTypes) {
      return _.sortBy(ceventTypes, function (ceventType) {
        return ceventType.name;
      });
    }

  }

  return studyCollectionDirective;
});
