/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 *
 */

import _ from 'lodash'
import angular from 'angular'

/*
 * An AngularJS service to open the modal.
 */
/* @ngInject */
class SpecimenAddModalService {

  constructor($uibModal) {
    Object.assign(this, { $uibModal })
  }

  /**
   * Creates a modal that allows the user to add one or more specimens to a collection event.
   *
   * @param {domain.Location[]} centreLocations - The locations belonging to the centres the specimens
   *        can be collected at.
   *
   * @param {domain.studies.CollectionSpecimenDescription[]} specimenDescriptions - the specimen
   *        specifications belonging to the collection event these specimen belong to.
   *
   * @param {Date} defaultDatetime - The default date to use for the specimens <code>time collected<code>.
   *
   * @return {$uibModal} A modal instance.
   *
   * When running the Travis CI build, jshint did not like that this function was named "open". Therefore,
   * it was renamed.
   */
  open(centreLocations, specimenDescriptions, defaultDatetime) {

    /*
     * The controller used by this modal.
     */
    /* @ngInject */
    class ModalController {

      constructor($scope,
                  $window,
                  $timeout,
                  $uibModalInstance,
                  Specimen,
                  timeService) {
        'ngInject'
        Object.assign(this, {
          $scope,
          $window,
          $timeout,
          $uibModalInstance,
          Specimen,
          timeService,
          centreLocations,
          specimenDescriptions,
          defaultDatetime
        }, {
          inventoryId:                 undefined,
          selectedSpecimenDescription: undefined,
          selectedLocationInfo:        undefined,
          amount:                      undefined,
          defaultAmount:               undefined,
          usingDefaultAmount:          true,
          timeCollected:               defaultDatetime,
          specimens:                   []
        })

        $scope.$watch('this.amount', () => {
          this.usingDefaultAmount = (_.isUndefined(this.defaultAmount) || (this.amount === this.defaultAmount));
        });
      }

      /*
       * Creates a new specimen based on values stored in the controller.
       */
      createSpecimen() {
        if (_.isUndefined(this.selectedSpecimenDescription)) {
          throw new Error('specimen type not selected');
        }

        return new this.Specimen(
          {
            inventoryId:        this.inventoryId,
            originLocationInfo: this.selectedLocationInfo,
            locationInfo:       this.selectedLocationInfo,
            timeCreated:        this.timeService.dateAndTimeToUtcString(this.timeCollected),
            amount:             this.amount
          },
          this.selectedSpecimenDescription);
      }

      /*
       * Called when the user presses the modal's OK button.
       */
      okPressed() {
        this.specimens.push(this.createSpecimen());
        this.$uibModalInstance.close(this.specimens);
      }

      /*
       * Called when the user presses the modal's NEXT button.
       */
      nextPressed() {
        this.specimens.push(this.createSpecimen());

        this.inventoryId          = undefined;
        this.selectedSpecimenDescription = undefined;
        this.amount               = undefined;
        this.defaultAmount        = undefined;
        this.usingDefaultAmount   = true;
        this.$scope.form.$setPristine();

        // Ensure focus returns to the specimen type drop down selection
        //
        // timeout makes sure that is invoked after any other event has been triggered.
        // e.g. click events that need to run before the focus or
        // inputs elements that are in a disabled state but are enabled when those events
        // are triggered.
        this.$timeout(() => {
          var element = this.$window.document.getElementById('specimenDescription');
          if (element) { element.focus(); }
        });
      }

      /*
       * Called when the user presses the modal's CANCEL button.
       */
      closePressed() {
        this.$uibModalInstance.dismiss('cancel');
      }

      /*
       * Called when the user updates Time Completed.
       */
      dateTimeOnEdit(datetime) {
        this.timeCompleted = datetime;
      }

      /*
       * Called when the user selects a new specimen type in the modal.
       */
      specimenDescriptionChanged() {
        if (this.selectedSpecimenDescription) {
          this.amount        = this.selectedSpecimenDescription.amount;
          this.defaultAmount = this.selectedSpecimenDescription.amount;
          this.units         = this.selectedSpecimenDescription.units;
        }
      }

      inventoryIdUpdated() {
        var alreadyEntered;

        if (!this.inventoryId) {
          this.$scope.form.inventoryId.$setValidity('inventoryIdEntered', true);
          this.$scope.form.inventoryId.$setValidity('inventoryIdTaken', true);
          return;
        }

        alreadyEntered = _.find(this.specimens, { inventoryId: this.inventoryId });

        this.$scope.form.inventoryId.$setValidity('inventoryIdEntered', !alreadyEntered);

        if (!alreadyEntered) {
          this.$scope.form.inventoryId.$setValidity('inventoryIdTaken', true);
          this.Specimen.getByInventoryId(this.inventoryId)
            .then(() => {
              this.$scope.form.inventoryId.$setValidity('inventoryIdTaken', false);
            })
            .catch(angular.noop);
        }
      }

    }

    var modalInstance = this.$uibModal.open({
      template: require('./specimenAddModal.html'),
      controller: ModalController,
      controllerAs: 'vm',
      backdrop: true,
      keyboard: false,
      modalFade: true
    });

    return modalInstance;
  }
}

export default ngModule => ngModule.service('specimenAddModal', SpecimenAddModalService)
