/**
 * AngularJS Component for {@link domain.centres.Centre Centre} administration.
 *
 * @namespace admin.centres.components.locationsPanel
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import angular from 'angular';

/*
 * Controller for this component.
 */
/* @ngInject */
function LocationsPanelController($scope,
                                  $state,
                                  gettextCatalog,
                                  domainNotificationService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.add     = add;
    vm.view    = view;
    vm.remove  = remove;

    // updates the selected tab in 'centreViewDirective' which is the parent directive
    $scope.$emit('tabbed-page-update', 'tab-selected');
  }

  function add() {
    // reload resolves in case centre's version has changed
    $state.go('home.admin.centres.centre.locations.locationAdd', {}, { reload: true });
  }

  function view(location) {
    $state.go('home.admin.centres.centre.locations.locationView', { locationSlug: location.slug });
  }

  function remove(location) {
    const doRemove = () => vm.centre.removeLocation(location)
          .then(centre => {
            vm.centre = centre;
          });

    domainNotificationService
      .removeEntity(doRemove,
                    gettextCatalog.getString('Remove Location'),
                    gettextCatalog.getString('Are you sure you want to remove location {{name}}?',
                                             { name: location.name}),
                    gettextCatalog.getString('Remove Failed'),
                    gettextCatalog.getString('Location {{name}} cannot be removed: ',
                                             { name: location.name}))
      .catch(angular.noop);
  }

}

/**
 * An AngularJS component that displays the {@link domain.Location Locations} associated with a {@link
 * domain.centres.Centre Centre}.
 *
 * The component allows for the user to view details for an individual location, add a new location, or remove
 * an existing location.
 *
 * @memberOf admin.centres.components.locationsPanel
 *
 * @param {domain.centres.Centre} centre - the centre to display locations for.
 */
const locationPanelComponent = {
  template: require('./locationsPanel.html'),
  controller: LocationsPanelController,
  controllerAs: 'vm',
  bindings: {
    centre: '<'
  }
};

export default ngModule => ngModule.component('locationsPanel', locationPanelComponent)
