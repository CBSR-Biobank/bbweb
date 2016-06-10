/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  /**
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

  CentreLocationViewCtrl.$inject = ['$state', 'modalInput', 'notificationsService'];

  function CentreLocationViewCtrl($state, modalInput, notificationsService) {
    var vm = this;

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
      return function (centre) {
        vm.centre = centre;
        vm.location = _.findWhere(vm.centre.locations, { uniqueId: vm.location.uniqueId });
        notificationsService.success(message, title, timeout);
      };
    }

    function editName() {
      modalInput.text(
        'Edit location name',
        'Name',
        vm.location.name,
        { required: true, minLength: 2 }
      ).result.then(function (name) {
        vm.location.name = name;
        vm.centre.updateLocation(vm.location)
          .then(postUpdate('Name changed successfully.',
                           'Change successful',
                           1500))
          .catch(notificationsService.updateError);
      });
    }

    function editStreet() {
      modalInput.text(
        'Edit street address',
        'Street address',
        vm.location.street
      ).result.then(function (street) {
        vm.location.street = street;
        vm.centre.updateLocation(vm.location)
          .then(postUpdate('Street address changed successfully.',
                           'Change successful',
                           1500))
          .catch(notificationsService.updateError);
      });
    }

    function editCity() {
      modalInput.text(
        'Edit city name',
        'City',
        vm.location.city
      ).result.then(function (city) {
        vm.location.city = city;
        vm.centre.updateLocation(vm.location)
          .then(postUpdate('City changed successfully.',
                           'Change successful',
                           1500))
          .catch(notificationsService.updateError);
      });
    }

    function editProvince() {
      modalInput.text(
        'Edit province',
        'Province',
        vm.location.province
      ).result.then(function (province) {
        vm.location.province = province;
        vm.centre.updateLocation(vm.location)
          .then(postUpdate('Province changed successfully.',
                           'Change successful',
                           1500))
          .catch(notificationsService.updateError);
      });
    }

    function editPostalCode() {
      modalInput.text(
        'Edit postal code',
        'Postal code',
        vm.location.postalCode
      ).result.then(function (postalCode) {
        vm.location.postalCode = postalCode;
        vm.centre.updateLocation(vm.location)
          .then(postUpdate('PostalCode changed successfully.',
                           'Change successful',
                           1500))
          .catch(notificationsService.updateError);
      });
    }

    function editPoBoxNumber() {
      modalInput.text(
        'Edit PO box number',
        'PO box Number',
        vm.location.postalCode
      ).result.then(function (poBoxNumber) {
        vm.location.poBoxNumber = poBoxNumber;
        vm.centre.updateLocation(vm.location)
          .then(postUpdate('PoBoxNumber changed successfully.',
                           'Change successful',
                           1500))
          .catch(notificationsService.updateError);
      });
    }

    function editCountryIsoCode() {
      modalInput.text(
        'Edit country ISO code',
        'Country ISO code',
        vm.location.postalCode
      ).result.then(function (countryIsoCode) {
        vm.location.countryIsoCode = countryIsoCode;
        vm.centre.updateLocation(vm.location)
          .then(postUpdate('CountryIsoCode changed successfully.',
                           'Change successful',
                           1500))
          .catch(notificationsService.updateError);
      });
    }

  }

  return centreLocationViewDirective;
});
