/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
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
    'domainEntityService',
    'tableService'
  ];

  /**
   *
   */
  function LocationsPanelCtrl($scope,
                              $state,
                              LocationViewer,
                              Panel,
                              domainEntityService,
                              tableService) {
    var vm = this;

    var panel = new Panel('centre.panel.locations', 'home.admin.centres.centre.locationAdd');

    vm.centre               = $scope.centre;
    vm.update               = update;
    vm.remove               = remove;
    vm.add                  = add;
    vm.information          = information;
    vm.panelOpen            = panel.getPanelOpenState();
    vm.modificationsAllowed = true; // not dependant on centre's state
    vm.tableParams          = tableService.getTableParamsWithCallback(getTableData,
                                                                      {},
                                                                      { counts: [] });

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
      var fakeEntity = {
        remove: function () {
          return vm.centre.removeLocation(location);
        }
      };

      domainEntityService.removeEntity(
        fakeEntity,
        'Remove Location',
        'Are you sure you want to remove location ' + location.name + '?',
        'Remove Failed',
        'Location ' + location.name + ' cannot be removed: ');

    }

    function getTableData() {
      return vm.centre.locations;
    }
  }

  return {
    directive: locationsPanelDirective,
    controller: LocationsPanelCtrl
  };
});
