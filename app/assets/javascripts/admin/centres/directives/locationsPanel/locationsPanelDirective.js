/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
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
    'gettext',
    'gettextCatalog',
    'LocationViewer',
    'domainNotificationService'
  ];

  /**
   *
   */
  function LocationsPanelCtrl($scope,
                              $state,
                              gettext,
                              gettextCatalog,
                              LocationViewer,
                              domainNotificationService) {
    var vm = this;

    vm.add    = add;
    vm.view   = view;
    vm.remove = remove;

    init();

    //--

    function init() {
      // updates the selected tab in 'centreViewDirective' which is the parent directive
      $scope.$emit('centre-view', 'locations-panel-selected');
    }

    function add() {
      // reload resolves in case centre's version has changed
      $state.go('home.admin.centres.centre.locations.locationAdd', {}, { reload: true });
    }

    function view(location) {
      $state.go('home.admin.centres.centre.locations.locationView', { uniqueId: location.uniqueId });
    }

    function remove(location) {
      domainNotificationService.removeEntity(
        doRemove,
        gettext('Remove Location'),
        gettextCatalog.getString('Are you sure you want to remove location {{name}}?',
                                 { name: location.name}),
        gettext('Remove Failed'),
        gettextCatalog.getString('Location {{name}} cannot be removed: ',
                                 { name: location.name}));

      function doRemove() {
        return vm.centre.removeLocation(location).then(function (centre) {
          vm.centre = centre;
        });
      }
    }

  }

  return locationsPanelDirective;
});
