/**
 * AngularJS Component for {@link domain.centres.Centre Centre} administration.
 *
 * @namespace admin.centres.components.centreLocationView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Controller for this component.
 */
/* @ngInject */
function CentreLocationViewController($state,
                                      gettextCatalog,
                                      modalInput,
                                      notificationsService,
                                      breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //----

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.centres'),
      breadcrumbService.forStateWithFunc(
        `home.admin.centres.centre.locations({ centreId: "${vm.centre.id}", locationId: "${vm.location.id}" })`,
        () => vm.centre.name),
      breadcrumbService.forStateWithFunc(
        'home.admin.centres.centre.locations.locationsView',
        () => vm.location.name)
    ];

    vm.back               = back;
    vm.editName           = editName;
    vm.editStreet         = editStreet;
    vm.editCity           = editCity;
    vm.editProvince       = editProvince;
    vm.editPostalCode     = editPostalCode;
    vm.editPoBoxNumber    = editPoBoxNumber;
    vm.editCountryIsoCode = editCountryIsoCode;
  }

  function back() {
    $state.go('home.admin.centres.centre.locations', {}, { reload: true });
  }

  function postUpdate(message, title, timeout) {
    timeout = timeout || 1500;
    return function (centre) {
      vm.centre = centre;
      vm.location = _.find(vm.centre.locations, { id: vm.location.id });
      notificationsService.success(message, title, timeout);
    };
  }

  function editName() {
    modalInput.text(gettextCatalog.getString('Edit location name'),
                    gettextCatalog.getString('Name'),
                    vm.location.name,
                    { required: true, minLength: 2 }).result
      .then(function (name) {
        vm.location.name = name;
        return vm.centre.updateLocation(vm.location)
      })
      .then(postUpdate(gettextCatalog.getString('Name changed successfully.'),
                       gettextCatalog.getString('Change successful')))
      .catch(notificationsService.updateError)
      .then(() => {
        // reload the state so that the URL gets updated
        $state.go($state.current.name,
                  { locationSlug: vm.location.slug },
                  { reload: true  })
      });
  }

  function editStreet() {
    modalInput.text(gettextCatalog.getString('Edit street address'),
                    gettextCatalog.getString('Street address'),
                    vm.location.street).result
      .then(function (street) {
        vm.location.street = street;
        vm.centre.updateLocation(vm.location)
          .then(postUpdate(gettextCatalog.getString('Street address changed successfully.'),
                           gettextCatalog.getString('Change successful')))
          .catch(notificationsService.updateError);
      });
  }

  function editCity() {
    modalInput.text(gettextCatalog.getString('Edit city name'),
                    gettextCatalog.getString('City'),
                    vm.location.city).result
      .then(function (city) {
        vm.location.city = city;
        vm.centre.updateLocation(vm.location)
          .then(postUpdate(gettextCatalog.getString('City changed successfully.'),
                           gettextCatalog.getString('Change successful')))
          .catch(notificationsService.updateError);
      });
  }

  function editProvince() {
    modalInput.text(gettextCatalog.getString('Edit province'),
                    gettextCatalog.getString('Province'),
                    vm.location.province).result
      .then(function (province) {
        vm.location.province = province;
        vm.centre.updateLocation(vm.location)
          .then(postUpdate(gettextCatalog.getString('Province changed successfully.'),
                           gettextCatalog.getString('Change successful')))
          .catch(notificationsService.updateError);
      });
  }

  function editPostalCode() {
    modalInput.text(gettextCatalog.getString('Edit postal code'),
                    gettextCatalog.getString('Postal code'),
                    vm.location.postalCode).result
      .then(function (postalCode) {
        vm.location.postalCode = postalCode;
        vm.centre.updateLocation(vm.location)
          .then(postUpdate(gettextCatalog.getString('PostalCode changed successfully.'),
                           gettextCatalog.getString('Change successful')))
          .catch(notificationsService.updateError);
      });
  }

  function editPoBoxNumber() {
    modalInput.text(gettextCatalog.getString('Edit PO box number'),
                    gettextCatalog.getString('PO box Number'),
                    vm.location.poBoxNumber).result
      .then(function (poBoxNumber) {
        vm.location.poBoxNumber = poBoxNumber;
        vm.centre.updateLocation(vm.location)
          .then(postUpdate(gettextCatalog.getString('PoBoxNumber changed successfully.'),
                           gettextCatalog.getString('Change successful')))
          .catch(notificationsService.updateError);
      });
  }

  function editCountryIsoCode() {
    modalInput.text(gettextCatalog.getString('Edit country ISO code'),
                    gettextCatalog.getString('Country ISO code'),
                    vm.location.countryIsoCode).result
      .then(function (countryIsoCode) {
        vm.location.countryIsoCode = countryIsoCode;
        vm.centre.updateLocation(vm.location)
          .then(postUpdate(gettextCatalog.getString('CountryIsoCode changed successfully.'),
                           gettextCatalog.getString('Change successful')))
          .catch(notificationsService.updateError);
      });
  }

}

/**
 * An AngularJS component that allows the user to view a {@link domain.Location Location} associated with a
 * {@link domain.centres.Centre Centre}.
 *
 * @memberOf admin.centres.components.centreLocationView
 *
 * @param {domain.centres.Centre} centre - the centre to add the location to.
 *
 * @param {domain.Location} location - the location to view.
 */
const centreLocationViewComponent = {
  template: require('./centreLocationView.html'),
  controller: CentreLocationViewController,
  controllerAs: 'vm',
  bindings: {
    centre:   '<',
    location: '<'
  }
};

export default ngModule => ngModule.component('centreLocationView', centreLocationViewComponent)
