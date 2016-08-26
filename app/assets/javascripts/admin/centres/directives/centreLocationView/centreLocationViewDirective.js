/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
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

  CentreLocationViewCtrl.$inject = [
    '$state',
    'gettext',
    'modalInput',
    'notificationsService'
  ];

  function CentreLocationViewCtrl($state,
                                  gettext,
                                  modalInput,
                                  notificationsService) {
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
      timeout = timeout || 1500;
      return function (centre) {
        vm.centre = centre;
        vm.location = _.find(vm.centre.locations, { uniqueId: vm.location.uniqueId });
        notificationsService.success(message, title, timeout);
      };
    }

    function editName() {
      modalInput.text(gettext('Edit location name'),
                      gettext('Name'),
                      vm.location.name,
                      { required: true, minLength: 2 }).result
        .then(function (name) {
          vm.location.name = name;
          vm.centre.updateLocation(vm.location)
            .then(postUpdate(gettext('Name changed successfully.'),
                             gettext('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

    function editStreet() {
      modalInput.text(gettext('Edit street address'),
                      gettext('Street address'),
                      vm.location.street).result
        .then(function (street) {
          vm.location.street = street;
          vm.centre.updateLocation(vm.location)
            .then(postUpdate(gettext('Street address changed successfully.'),
                             gettext('Change successful')))
            .catch(notificationsService.updateError);
      });
    }

    function editCity() {
      modalInput.text(gettext('Edit city name'),
                      gettext('City'),
                      vm.location.city).result
        .then(function (city) {
          vm.location.city = city;
          vm.centre.updateLocation(vm.location)
            .then(postUpdate(gettext('City changed successfully.'),
                             gettext('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

    function editProvince() {
      modalInput.text(gettext('Edit province'),
                      gettext('Province'),
                      vm.location.province).result
        .then(function (province) {
          vm.location.province = province;
          vm.centre.updateLocation(vm.location)
            .then(postUpdate(gettext('Province changed successfully.'),
                             gettext('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

    function editPostalCode() {
      modalInput.text(gettext('Edit postal code'),
                      gettext('Postal code'),
                      vm.location.postalCode).result
        .then(function (postalCode) {
          vm.location.postalCode = postalCode;
          vm.centre.updateLocation(vm.location)
            .then(postUpdate(gettext('PostalCode changed successfully.'),
                             gettext('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

    function editPoBoxNumber() {
      modalInput.text(gettext('Edit PO box number'),
                      gettext('PO box Number'),
                      vm.location.postalCode).result
        .then(function (poBoxNumber) {
          vm.location.poBoxNumber = poBoxNumber;
          vm.centre.updateLocation(vm.location)
            .then(postUpdate(gettext('PoBoxNumber changed successfully.'),
                             gettext('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

    function editCountryIsoCode() {
      modalInput.text(gettext('Edit country ISO code'),
                      gettext('Country ISO code'),
                      vm.location.postalCode).result.
        then(function (countryIsoCode) {
          vm.location.countryIsoCode = countryIsoCode;
          vm.centre.updateLocation(vm.location)
            .then(postUpdate(gettext('CountryIsoCode changed successfully.'),
                             gettext('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

  }

  return centreLocationViewDirective;
});
