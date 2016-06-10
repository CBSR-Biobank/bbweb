/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  /**
   *
   */
  function locationsPanelDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        centre: '='
      },
      templateUrl: '/assets/javascripts/admin/centres/directives/locationsPanel/locationsPanel.html',
      controller: LocationsPanelCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  LocationsPanelCtrl.$inject = [
    '$scope',
    '$state',
    'LocationViewer',
    'domainEntityService'
  ];

  /**
   *
   */
  function LocationsPanelCtrl($scope,
                              $state,
                              LocationViewer,
                              domainEntityService) {
    var vm = this;

    vm.add    = add;
    vm.view   = view;
    vm.remove = remove;

    //---

    function add() {
      // reload resolves in case centre's version has changed
      $state.go('home.admin.centres.centre.locations.locationAdd', {}, { reload: true });
    }

    function view(location) {
      $state.go('home.admin.centres.centre.locations.locationView', { uniqueId: location.uniqueId });
    }

    function remove(location) {
      domainEntityService.removeEntity(
        doRemove,
        'Remove Location',
        'Are you sure you want to remove location ' + location.name + '?',
        'Remove Failed',
        'Location ' + location.name + ' cannot be removed: ');

      function doRemove() {
        return vm.centre.removeLocation(location).then(function (centre) {
          vm.centre = centre;
        });
      }
    }

  }

  return locationsPanelDirective;
});
