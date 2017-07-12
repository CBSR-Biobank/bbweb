/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/admin/users/components/nameEmailStateFilters/nameEmailStateFilters.html',
    controller: NameAndStateFiltersController,
    controllerAs: 'vm',
    bindings: {
      stateData:            '<',
      onNameFilterUpdated:  '&',
      onEmailFilterUpdated:  '&',
      onStateFilterUpdated: '&',
      onFiltersCleared:     '&'
    }
  };

  NameAndStateFiltersController.$inject = ['$controller'];

  /*
   * Controller for this component.
   */
  function NameAndStateFiltersController($controller) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      // initialize this controller's base class
      $controller('NameAndStateFiltersController', { vm: vm });

      vm.emailFilter = '';
      vm.emailFilterUpdated = emailFilterUpdated;
      vm.superClearFilters = vm.clearFilters;
      vm.clearFilters = clearFilters;
    }

    function emailFilterUpdated() {
      vm.onEmailFilterUpdated()(vm.emailFilter);
    }

    function clearFilters() {
      vm.emailFilter = '';
      vm.superClearFilters();
    }
  }

  return component;
});
