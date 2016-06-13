/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 *
 * --------------------
 *
 * TODO: do not allow duplicate inventory IDs to be entered
 *
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      _       = require('underscore'),
      name    = 'biobank.specimenAddModal',
      module;

  /**
   * Creates a module with one service and a controller that allows the user to add one or more specimens
   * to a collection event using a modal.
   */
  module = angular.module(name, []);
  module.service('specimenAddModal', specimenAddModalService);
  module.controller('specimenAddModal.ModalInstanceController', ModalInstanceController);

  specimenAddModalService.$inject = ['$uibModal'];

  /**
   * An AngularJS service to open the modal.
   */
  function specimenAddModalService($uibModal) {
    var service = {
      open: open
    };
    return service;

    //-------

    /**
     * Creates a modal that allows the user add one or more specimens to a collection event.
     */
    function open(centreLocations, specimenSpecs) {
      var modalInstance = $uibModal.open({
        templateUrl: '/assets/javascripts/collection/specimenAdd/specimenAdd.html',
        controller: 'specimenAddModal.ModalInstanceController',
        controllerAs: 'vm',
        backdrop: 'static',
        keyboard: false,
        resolve: {
          centreLocations: function () { return centreLocations; },
          specimenSpecs: function () { return specimenSpecs; }
        }
      });

      return modalInstance;
    }

  }

  ModalInstanceController.$inject = [
    '$scope',
    '$window',
    '$timeout',
    '$uibModalInstance',
    'bbwebConfig',
    'Specimen',
    'timeService',
    'centreLocations',
    'specimenSpecs'
  ];

  /**
   * The controller used by this modal.
   */
  function ModalInstanceController($scope,
                                   $window,
                                   $timeout,
                                   $uibModalInstance,
                                   bbwebConfig,
                                   Specimen,
                                   timeService,
                                   centreLocations,
                                   specimenSpecs) {
    var vm = this;

    vm.inventoryId          = undefined;
    vm.selectedSpecimenSpec = undefined;
    vm.selectedLocationId   = undefined;
    vm.amount               = undefined;
    vm.defaultAmount        = undefined;
    vm.centreLocations      = centreLocationsInit(centreLocations);
    vm.specimenSpecs        = specimenSpecs;
    vm.usingDefaultAmount   = true;
    vm.timeCollected        = new Date();
    vm.datetimePickerFormat = bbwebConfig.datepickerFormat;
    vm.timepickerOptions    = { readonlyInput: false, showMeridian: false };
    vm.calendarOpen         = false;
    vm.specimens            = [];

    vm.okPressed    = okPressed;
    vm.nextPressed  = nextPressed;
    vm.closePressed = closePressed;
    vm.openCalendar = openCalendar;
    vm.specimenSpecChanged = specimenSpecChanged;
    vm.inventoryIdUpdated = inventoryIdUpdated;

    $scope.$watch('vm.amount', function () {
      vm.usingDefaultAmount = (_.isUndefined(vm.defaultAmount) || (vm.amount === vm.defaultAmount));
    });

    //--

    /**
     * Concatenates the centre name and location name so that they can be selected from a
     * drop down list.
     */
    function centreLocationsInit(centreLocations) {
      return _.map(centreLocations, function (centreLocation) {
        return _.extend({ name: centreLocation.centreName + ': ' + centreLocation.locationName },
                        _.pick(centreLocation, 'centreId', 'locationId'));
      });
    }

    /**
     * Creates a new specimen based on values stored in the controller.
     */
    function createSpecimen() {
      if (_.isUndefined(vm.selectedSpecimenSpec)) {
        throw new Error('specimen type not selected');
      }

      return new Specimen(
        {
          inventoryId:      vm.inventoryId,
          originLocationId: vm.selectedLocationId,
          locationId:       vm.selectedLocationId,
          timeCreated:      timeService.dateToUtcString(vm.timeCollected),
          amount:           vm.amount
        },
        vm.selectedSpecimenSpec);
    }

    /**
     * Called when the user presses the modal's OK button.
     */
    function okPressed() {
      vm.specimens.push(createSpecimen());
      $uibModalInstance.close(vm.specimens);
    }

    /**
     * Called when the user presses the modal's NEXT button.
     */
    function nextPressed() {
      vm.specimens.push(createSpecimen());

      vm.inventoryId          = undefined;
      vm.selectedSpecimenSpec = undefined;
      vm.amount               = undefined;
      vm.defaultAmount        = undefined;
      vm.usingDefaultAmount   = true;
      $scope.form.$setPristine();

      // Ensure focus returns to the specimen type drop down selection
      //
      // timeout makes sure that is invoked after any other event has been triggered.
      // e.g. click events that need to run before the focus or
      // inputs elements that are in a disabled state but are enabled when those events
      // are triggered.
      $timeout(function() {
        var element = $window.document.getElementById('specimenSpec');
        if (element) { element.focus(); }
      });
    }

    /**
     * Called when the user presses the modal's CANCEL button.
     */
    function closePressed() {
      $uibModalInstance.dismiss('cancel');
    }

    /**
     * Called when the user presses the modal's calendar button.
     */
    function openCalendar(e) {
      vm.calendarOpen = true;
    }

    /**
     * Called when the user selects a new specimen type in the modal.
     */
    function specimenSpecChanged() {
      if (vm.selectedSpecimenSpec) {
        vm.amount        = vm.selectedSpecimenSpec.amount;
        vm.defaultAmount = vm.selectedSpecimenSpec.amount;
        vm.units         = vm.selectedSpecimenSpec.units;
      }
    }

    function inventoryIdUpdated() {
      var alreadyEntered;

      if (!vm.inventoryId) {
        $scope.form.inventoryId.$setValidity('inventoryIdEntered', true);
        $scope.form.inventoryId.$setValidity('inventoryIdTaken', true);
        return;
      }

      alreadyEntered = _.findWhere(vm.specimens, { inventoryId: vm.inventoryId });

      $scope.form.inventoryId.$setValidity('inventoryIdEntered', !alreadyEntered);

      if (!alreadyEntered) {
        $scope.form.inventoryId.$setValidity('inventoryIdTaken', true);
        Specimen.getByInventoryId(vm.inventoryId)
          .then(function (specimen) {
            $scope.form.inventoryId.$setValidity('inventoryIdTaken', false);
          });
      }
    }

  }

  return {
    name: name,
    module: module
  };

});
