/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 *
 */

import _ from 'lodash'
import angular from 'angular'

let service;

/**
 * An AngularJS service that lets the user open a modal dialog box.
 *
 * @memberOf collection.services
 */
class SpecimenAddModalService {

  constructor($uibModal,
              $window,
              $timeout,
              Specimen,
              timeService) {
    'ngInject';
    Object.assign(this,
                  {
                    $uibModal,
                    $window,
                    $timeout,
                    Specimen,
                    timeService
                  })
    service = this;
  }

  /**
   * Creates a modal that allows the user to add one or more {@link domain.partcipants.Specimen Specimens} to
   * a {@link domain.partcipant.CollectionEvent CollectionEvent}.
   *
   * When the user presses the `OK` button, the specimens that were added are returned as an array.
   *
   * @param {domain.Location[]} centreLocations - The locations belonging to the centres the specimens can be
   * collected at.
   *
   * @param {domain.studies.CollectionSpecimenDefinition[]} specimenDefinitions - the *Specimen
   *        Specifications* belonging to the *Collection Event* these specimen belong to.
   *
   * @param {Date} defaultDatetime - The default date to use for the specimen's {@link
   * domain.participants.Specimen#timeCreated timeCreated}.
   *
   * @return {object} The "UI Bootstrap" modal instance.
   */
  open(centreLocations, specimenDefinitions, defaultDatetime) {

    /*
     * The controller used by this modal.
     */
    /* @ngInject */
    class ModalController {

      constructor($uibModalInstance, $scope) {
        'ngInject'
        Object.assign(this,
                      {
                        $uibModalInstance,
                        $scope,
                        centreLocations,
                        specimenDefinitions,
                        defaultDatetime
                      },
                      {
                        inventoryId:                 undefined,
                        selectedSpecimenDefinition: undefined,
                        selectedLocationInfo:        undefined,
                        amount:                      undefined,
                        defaultAmount:               undefined,
                        usingDefaultAmount:          true,
                        timeCollected:               defaultDatetime,
                        specimens:                   []
                      })

        this.$scope.$watch('this.amount', () => {
          this.usingDefaultAmount =
            (_.isUndefined(this.defaultAmount) || (this.amount === this.defaultAmount));
        });
      }

      /*
       * Creates a new specimen based on values stored in the controller.
       */
      createSpecimen() {
        if (_.isUndefined(this.selectedSpecimenDefinition)) {
          throw new Error('specimen type not selected');
        }

        return new service.Specimen(
          {
            inventoryId:        this.inventoryId,
            originLocationInfo: this.selectedLocationInfo,
            locationInfo:       this.selectedLocationInfo,
            timeCreated:        service.timeService.dateAndTimeToUtcString(this.timeCollected),
            amount:             this.amount
          },
          this.selectedSpecimenDefinition);
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

        this.inventoryId                 = undefined;
        this.selectedSpecimenDefinition = undefined;
        this.amount                      = undefined;
        this.defaultAmount               = undefined;
        this.usingDefaultAmount          = true;
        this.$scope.form.$setPristine();

        // Ensure focus returns to the specimen type drop down selection
        //
        // timeout makes sure that is invoked after any other event has been triggered.
        // e.g. click events that need to run before the focus or
        // inputs elements that are in a disabled state but are enabled when those events
        // are triggered.
        service.$timeout(() => {
          const element = service.$window.document.getElementById('specimenDefinition');
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
      specimenDefinitionChanged() {
        if (this.selectedSpecimenDefinition) {
          this.amount        = this.selectedSpecimenDefinition.amount;
          this.defaultAmount = this.selectedSpecimenDefinition.amount;
          this.units         = this.selectedSpecimenDefinition.units;
        }
      }

      inventoryIdUpdated() {
        if (!this.inventoryId) {
          this.$scope.form.inventoryId.$setValidity('inventoryIdEntered', true);
          this.$scope.form.inventoryId.$setValidity('inventoryIdTaken', true);
          return;
        }

        const alreadyEntered = _.find(this.specimens, { inventoryId: this.inventoryId });

        this.$scope.form.inventoryId.$setValidity('inventoryIdEntered', !alreadyEntered);

        if (!alreadyEntered) {
          this.$scope.form.inventoryId.$setValidity('inventoryIdTaken', true);
          service.Specimen.getByInventoryId(this.inventoryId)
            .then(() => {
              this.$scope.form.inventoryId.$setValidity('inventoryIdTaken', false);
            })
            .catch(angular.noop);
        }
      }

    }

    const modal = this.$uibModal.open({
      template: require('./specimenAddModal.html'),
      controller: ModalController,
      controllerAs: 'vm',
      backdrop: true,
      keyboard: false,
      modalFade: true
    });

    return modal;
  }
}

export default ngModule => ngModule.service('specimenAddModal', SpecimenAddModalService)
