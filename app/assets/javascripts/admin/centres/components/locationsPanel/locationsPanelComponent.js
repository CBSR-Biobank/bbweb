/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/admin/centres/components/locationsPanel/locationsPanel.html',
    controller: LocationsPanelController,
    controllerAs: 'vm',
    bindings: {
      centre: '='
    }
  };

  LocationsPanelController.$inject = [
    '$scope',
    '$state',
    'gettextCatalog',
    'domainNotificationService'
  ];

  /*
   * Controller for this component.
   */
  function LocationsPanelController($scope,
                                    $state,
                                    gettextCatalog,
                                    domainNotificationService) {
    var vm = this;

    vm.$onInit = onInit;
    vm.add     = add;
    vm.view    = view;
    vm.remove  = remove;

    //--

    function onInit() {
      // updates the selected tab in 'centreViewDirective' which is the parent directive
      $scope.$emit('tabbed-page-update', 'tab-selected');
    }

    function add() {
      // reload resolves in case centre's version has changed
      $state.go('home.admin.centres.centre.locations.locationAdd', {}, { reload: true });
    }

    function view(location) {
      $state.go('home.admin.centres.centre.locations.locationView', { locationId: location.id });
    }

    function remove(location) {
      domainNotificationService.removeEntity(
        doRemove,
        gettextCatalog.getString('Remove Location'),
        gettextCatalog.getString('Are you sure you want to remove location {{name}}?',
                                 { name: location.name}),
        gettextCatalog.getString('Remove Failed'),
        gettextCatalog.getString('Location {{name}} cannot be removed: ',
                                 { name: location.name}));

      function doRemove() {
        return vm.centre.removeLocation(location).then(function (centre) {
          vm.centre = centre;
        });
      }
    }

  }

  return component;
});
