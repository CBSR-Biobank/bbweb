define(['../module'], function(module) {
  'use strict';

  module.controller('LocationsPanelCtrl', LocationsPanelCtrl);

  LocationsPanelCtrl.$inject = [
    '$scope',
    '$state',
    'panelService',
    'domainEntityModalService',
    'domainEntityRemoveService',
    'centreLocationService'
  ];

  /**
   *
   */
  function LocationsPanelCtrl($scope,
                              $state,
                              panelService,
                              domainEntityModalService,
                              domainEntityRemoveService,
                              centreLocationService) {
    var vm = this;

    var helper = panelService.panel(
      'centre.panel.locations',
      'admin.centres.centre.locationAdd');

    vm.centre      = $scope.centre;
    vm.locations   = $scope.locations;
    vm.update      = update;
    vm.remove      = remove;
    vm.add         = helper.add;
    vm.information = information;
    vm.panelOpen   = helper.panelOpen;
    vm.panelToggle = helper.panelToggle;

    vm.tableParams = helper.getTableParams(vm.locations);
    vm.tableParams.settings().$scope = $scope;  // kludge: see https://github.com/esvit/ng-table/issues/297#issuecomment-55756473

    function information(location) {
      var title = 'Location';
      var data = [
        {name: 'Name:',              value: location.name},
        {name: 'Street:',            value: location.street},
        {name: 'City:',              value: location.city},
        {name: 'Province / State:',  value: location.province},
        {name: 'Postal / Zip code:', value: location.postalCode},
        {name: 'PO Box Number:',     value: location.poBoxNumber},
        {name: 'Country ISO Code:',  value: location.countryIsoCode},
      ];
      domainEntityModalService.show(title, data);
    }

    function update(location) {
      $state.go('admin.centres.centre.locationUpdate', { locationId: location.id });
    }

    function remove(location) {
      domainEntityRemoveService.remove(
        'Remove Location',
        'Are you sure you want to remove location ' + location.name + '?',
        'Location ' + location.name + ' cannot be removed: ',
        removeCallback,
        location,
        'admin.studies.cetnre.locations');

      function removeCallback(location) {
        return centreLocationService.remove(vm.centre.id, location.id);
      }
    }
  }

});
