define(['../module'], function(module) {
  'use strict';

  module.directive('locationsPanel', locationsPanel);

  /**
   *
   */
  function locationsPanel() {
    var directive = {
      require: '^tab',
      restrict: 'EA',
      scope: {
        centre: '=',
        locations: '='
      },
      templateUrl: '/assets/javascripts/admin/centres/locationsPanel.html',
      controller: 'LocationsPanelCtrl as vm'
    };
    return directive;
  }

  module.controller('LocationsPanelCtrl', LocationsPanelCtrl);

  LocationsPanelCtrl.$inject = [
    '$scope',
    '$state',
    'LocationViewer',
    'Panel',
    'domainEntityRemoveService',
    'centreLocationService'
  ];

  /**
   *
   */
  function LocationsPanelCtrl($scope,
                              $state,
                              LocationViewer,
                              Panel,
                              domainEntityRemoveService,
                              centreLocationService) {
    var vm = this;

    var helper = new Panel('centre.panel.locations', 'home.admin.centres.centre.locationAdd');

    vm.centre           = $scope.centre;
    vm.locations        = $scope.locations;
    vm.update           = update;
    vm.remove           = remove;
    vm.add              = add;
    vm.information      = information;
    vm.panelOpen        = helper.panelOpen;
    vm.panelToggle      = panelToggle;

    vm.tableParams      = helper.getTableParams(vm.locations);

    vm.modificationsAllowed = vm.centre.status === 'Disabled';

    function add() {
      return helper.add();
    }

    function panelToggle() {
      return helper.panelToggle();
    }

    function information(location) {
      return new LocationViewer(location);
    }

    function update(location) {
      $state.go('home.admin.centres.centre.locationUpdate', { locationId: location.id });
    }

    function remove(location) {
      domainEntityRemoveService.remove(
        'Remove Location',
        'Are you sure you want to remove location ' + location.name + '?',
        'Location ' + location.name + ' cannot be removed: ',
        removeCallback,
        location,
        'home.admin.centres.centre.locations');

      function removeCallback(location) {
        return centreLocationService.remove(vm.centre.id, location.id);
      }
    }
  }

});
