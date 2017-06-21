/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  //NameAndStateFiltersController.$inject = [];

  /*
   * Controller base class used by nameAndStateFiltersComponent and nameEmailStateFiltersComponent.
   */
  function NameAndStateFiltersController(vm) {
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

  return NameAndStateFiltersController;
});
