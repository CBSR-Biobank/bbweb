/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/common/components/nameAndStateFilters/nameAndStateFilters.html',
    controller: NameAndStateFiltersController,
    controllerAs: 'vm',
    bindings: {
      stateData:            '<',
      onNameFilterUpdated:  '&',
      onStateFilterUpdated: '&',
      onFiltersCleared:     '&'
    }
  };

  NameAndStateFiltersController.$inject = [];

  /*
   * Controller for this component.
   */
  function NameAndStateFiltersController() {
    var vm = this;

    vm.nameFilter = '';
    vm.selectedState = 'all';
    vm.stateData.unshift({ id: 'all', label: 'All' });

    vm.nameFilterUpdated = nameFilterUpdated;
    vm.stateFilterUpdated = stateFilterUpdated;
    vm.clearFilters = clearFilters;

    //--

    function nameFilterUpdated() {
      vm.onNameFilterUpdated()(vm.nameFilter);
    }

    function stateFilterUpdated() {
      vm.onStateFilterUpdated()(vm.selectedState);
    }

    function clearFilters() {
      vm.nameFilter = '';
      vm.selectedState = 'all';
      vm.onFiltersCleared()();
    }
  }

  return component;
});
