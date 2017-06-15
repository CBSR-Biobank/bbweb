/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _       = require('lodash'),
      sprintf = require('sprintf-js').sprintf;

  /*
   *
   */
  function centreLocationViewDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        centre: '=',
        location: '='
      },
      templateUrl : '/assets/javascripts/admin/centres/directives/centreLocationView/centreLocationView.html',
      controller: CentreLocationViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CentreLocationViewCtrl.$inject = [
    '$state',
    'gettextCatalog',
    'modalInput',
    'notificationsService',
    'breadcrumbService'
  ];

  function CentreLocationViewCtrl($state,
                                  gettextCatalog,
                                  modalInput,
                                  notificationsService,
                                  breadcrumbService) {
    var vm = this;

    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.centres'),
      breadcrumbService.forStateWithFunc(
        sprintf('home.admin.centres.centre.locations({ centreId: "%s", locationId: "%s" })',
                vm.centre.id,
                vm.location.id),
        function () { return vm.centre.name; }),
      breadcrumbService.forStateWithFunc(
        'home.admin.centres.centre.locations.locationsView',
        function () { return vm.location.name; })
    ];

    vm.back               = back;
    vm.editName           = editName;
    vm.editStreet         = editStreet;
    vm.editCity           = editCity;
    vm.editProvince       = editProvince;
    vm.editPostalCode     = editPostalCode;
    vm.editPoBoxNumber    = editPoBoxNumber;
    vm.editCountryIsoCode = editCountryIsoCode;

    //----

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
          vm.centre.updateLocation(vm.location)
            .then(postUpdate(gettextCatalog.getString('Name changed successfully.'),
                         gettextCatalog.getString('Change successful')))
            .catch(notificationsService.updateError);
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

  return centreLocationViewDirective;
});
