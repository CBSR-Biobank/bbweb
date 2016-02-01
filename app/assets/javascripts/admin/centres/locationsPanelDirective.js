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

    vm.centre               = $scope.centre;
    vm.update               = update;
    vm.remove               = remove;
    vm.add                  = add;
    vm.information          = information;
    vm.panelOpen            = panel.getPanelOpenState();
    vm.modificationsAllowed = true; // not dependant on centre's state

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

  }

  return {
    directive: locationsPanelDirective,
    controller: LocationsPanelCtrl
  };
});
