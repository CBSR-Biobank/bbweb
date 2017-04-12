/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 *
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  specimenAddModalService.$inject = ['$uibModal'];

  /*
   * An AngularJS service to open the modal.
   */
  function specimenAddModalService($uibModal) {
    var service = {
      open: openModal
    };
    return service;

    //-------

    /**
     * Creates a modal that allows the user to add one or more specimens to a collection event.
     *
     * @param {domain.Location[]} centreLocations - The locations belonging to the centres the specimens
     *        can be collected at.
     *
     * @param {domain.studies.CollectionSpecimenSpec[]} specimenSpecs - the specimen specifications belonging
     *        to the collection event these specimen belong to.
     *
     * @param {Date} defaultDatetime - The default date to use for the specimens <code>time collected<code>.
     *
     * @return {$uibModal} A modal instance.
     *
     * When running the Travis CI build, jshint did not like that this function was named "open". Therefore,
     * it was renamed.
     */
    function openModal(centreLocations, specimenSpecs, defaultDatetime) {
      var modalInstance = $uibModal.open({
        templateUrl: '/assets/javascripts/collection/services/specimenAddModal/specimenAddModal.html',
        controller: ModalInstanceController,
        controllerAs: 'vm',
        backdrop: true,
        keyboard: false,
        modalFade: true
      });

      ModalInstanceController.$inject = [
        '$scope',
        '$window',
        '$timeout',
        '$uibModalInstance',
        'AppConfig',
        'Specimen',
        'timeService'
      ];

      return modalInstance;

      //---

      /*
       * The controller used by this modal.
       */
      function ModalInstanceController($scope,
                                       $window,
                                       $timeout,
                                       $uibModalInstance,
                                       AppConfig,
                                       Specimen,
                                       timeService) {
        var vm = this;

        vm.inventoryId          = undefined;
        vm.selectedSpecimenSpec = undefined;
        vm.selectedLocationInfo = undefined;
        vm.amount               = undefined;
        vm.defaultAmount        = undefined;
        vm.centreLocations      = centreLocations;
        vm.specimenSpecs        = specimenSpecs;
        vm.usingDefaultAmount   = true;
        vm.timeCollected        = defaultDatetime;
        vm.specimens            = [];

        vm.okPressed            = okPressed;
        vm.nextPressed          = nextPressed;
        vm.closePressed         = closePressed;
        vm.dateTimeOnEdit       = dateTimeOnEdit;
        vm.specimenSpecChanged  = specimenSpecChanged;
        vm.inventoryIdUpdated   = inventoryIdUpdated;

        $scope.$watch('vm.amount', function () {
          vm.usingDefaultAmount = (_.isUndefined(vm.defaultAmount) || (vm.amount === vm.defaultAmount));
        });

        //--

        /*
         * Creates a new specimen based on values stored in the controller.
         */
        function createSpecimen() {
          if (_.isUndefined(vm.selectedSpecimenSpec)) {
            throw new Error('specimen type not selected');
          }

          return new Specimen(
            {
              inventoryId:        vm.inventoryId,
              originLocationInfo: vm.selectedLocationInfo,
              locationInfo:       vm.selectedLocationInfo,
              timeCreated:        timeService.dateAndTimeToUtcString(vm.timeCollected),
              amount:             vm.amount
            },
            vm.selectedSpecimenSpec);
        }

        /*
         * Called when the user presses the modal's OK button.
         */
        function okPressed() {
          vm.specimens.push(createSpecimen());
          $uibModalInstance.close(vm.specimens);
        }

        /*
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

        /*
         * Called when the user presses the modal's CANCEL button.
         */
        function closePressed() {
          $uibModalInstance.dismiss('cancel');
        }

        /*
         * Called when the user updates Time Completed.
         */
        function dateTimeOnEdit(datetime) {
          vm.timeCompleted = datetime;
        }

        /*
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

          alreadyEntered = _.find(vm.specimens, { inventoryId: vm.inventoryId });

          $scope.form.inventoryId.$setValidity('inventoryIdEntered', !alreadyEntered);

          if (!alreadyEntered) {
            $scope.form.inventoryId.$setValidity('inventoryIdTaken', true);
            Specimen.getByInventoryId(vm.inventoryId)
              .then(function () {
                $scope.form.inventoryId.$setValidity('inventoryIdTaken', false);
              });
          }
        }

      }
    }
  }

  return specimenAddModalService;
});
