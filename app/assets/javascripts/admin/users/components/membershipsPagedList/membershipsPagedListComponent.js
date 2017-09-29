/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Displays studies in a panel list.
   *
   * @return {object} An AngularJS component.
   */
  var component = {
    template: require('./membershipsPagedList.html'),
    controller: Controller,
    controllerAs: 'vm',
    bindings: {
    }
  };

  Controller.$inject = [
    '$controller',
    '$scope',
    '$state',
    'Membership',
    'gettextCatalog',
    'NameFilter'
  ];

  /*
   * Controller for this component.
   */
  function Controller($controller,
                      $scope,
                      $state,
                      Membership,
                      gettextCatalog,
                      NameFilter) {
    var vm = this;
    vm.$onInit = onInit;
    vm.filters = {};
    vm.filters[NameFilter.name] = new NameFilter();
    vm.onFiltersCleared = filterCleared;
    vm.nameFilterCleared = false;

    //--

    function onInit() {
      vm.counts      = { total: 1 };
      vm.limit       = 5;
      vm.getItems    = getItems;
      vm.getItemIcon = getItemIcon;

      // initialize this controller's base class
      $controller('PagedListController', {
        vm:             vm,
        $state:         $state,
        gettextCatalog: gettextCatalog
      });
    }

    function getItems(options) {
      return Membership.list(options);
    }

    function getItemIcon() {
      return 'glyphicon-cog';
    }

    function filterCleared() {
      vm.nameFilterCleared = !vm.nameFilterCleared;
    }

  }

  return component;
});
