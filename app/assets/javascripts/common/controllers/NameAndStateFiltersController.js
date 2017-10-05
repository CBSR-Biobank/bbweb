/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  NameAndStateFiltersController.$inject = ['vm'];

  /*
   * Controller base class used by nameAndStateFiltersComponent and nameEmailStateFiltersComponent.
   */
  function NameAndStateFiltersController(vm) {
    vm.nameFilter = '';
    vm.selectedState = 'all';

    vm.nameFilterUpdated = nameFilterUpdated;
    vm.stateFilterUpdated = stateFilterUpdated;
    vm.clearFilters = clearFilters;

    //--

    function nameFilterUpdated(value) {
      vm.nameFilter = value;
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
