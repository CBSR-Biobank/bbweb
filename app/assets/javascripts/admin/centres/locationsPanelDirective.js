define(['angular'], function(angular) {
  'use strict';

  /**
   *
   */
  function locationsPanelDirective() {
    return {
      require: '^tab',
      restrict: 'EA',
      scope: {
        centre: '=',
        locations: '='
      },
      templateUrl: '/assets/javascripts/admin/centres/locationsPanel.html',
      controller: 'LocationsPanelCtrl as vm'
    };
  }

  LocationsPanelCtrl.$inject = [
    '$scope',
    '$state',
    'LocationViewer',
    'Panel',
    'domainEntityService'
  ];

  /**
   *
   */
  function LocationsPanelCtrl($scope,
                              $state,
                              LocationViewer,
                              Panel,
                              domainEntityService) {
    var vm = this;

    var panel = new Panel('centre.panel.locations', 'home.admin.centres.centre.locationAdd');

    vm.centre           = $scope.centre;
    vm.update           = update;
    vm.remove           = remove;
    vm.add              = add;
    vm.information      = information;
    vm.panelOpen        = panel.getPanelOpenState();
    vm.tableParams      = panel.getTableParams(vm.centre.locations);

    vm.modificationsAllowed = vm.centre.status === 'Disabled';

    $scope.$watch(angular.bind(vm, function() { return vm.panelOpen; }),
                  angular.bind(panel, panel.watchPanelOpenChangeFunc));

    function add() {
      return panel.add();
    }

    function information(location) {
      return new LocationViewer(location);
    }

    function update(location) {
      $state.go('home.admin.centres.centre.locationUpdate', { locationId: location.id });
    }

    function remove(location) {
      domainEntityService.removeEntity(
        removeCallback,
        'Remove Location',
        'Are you sure you want to remove location ' + location.name + '?',
        'Remove Failed',
        'Location ' + location.name + ' cannot be removed: ');

      function removeCallback(location) {
        return vm.centre.removeLocation(location);
      }
    }
  }

  return {
    directive: locationsPanelDirective,
    controller: LocationsPanelCtrl
  };
});
