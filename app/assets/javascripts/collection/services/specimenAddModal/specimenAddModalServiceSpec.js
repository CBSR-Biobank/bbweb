/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ModalTestSuiteMixin } from 'test/mixins/ModalTestSuiteMixin';
import _ from 'lodash';
import faker  from 'faker';
import ngModule from '../../index'

describe('Service: specimenAddModal', function() {

  beforeEach(() => {
    angular.mock.module('ngAnimateMock', ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ModalTestSuiteMixin);

      this.injectDependencies('$uibModal',
                              '$q',
                              '$rootScope',
                              '$document',
                              'specimenAddModal',
                              'AppConfig',
                              'Specimen',
                              'timeService',
                              'Factory');
      this.addModalMatchers();

      this.openModal = (centreLocations = [],
                        specimenDefinitions = [],
                        defaultDatetime = new Date()) => {
        this.modal = this.specimenAddModal.open(centreLocations, specimenDefinitions, defaultDatetime);
        this.modal.result.then(angular.noop, angular.noop);
        this.$rootScope.$digest();
        this.modalElement = this.modalElementFind();
        this.scope = this.modalElement.scope();

        this.scope.form = {
          $setPristine: jasmine.createSpy('modalInstance.form'),
          inventoryId: { $setValidity: jasmine.createSpy('modalInstance.form.inventoryId') }
        };
      };

      this.createCentreLocations = () => {
        var centres =_.range(2).map(() => {
          var locations = _.range(2).map(() => this.Factory.location());
          return this.Factory.centre({ locations: locations });
        });
        return this.Factory.centreLocations(centres);
      };

      this.createSpecimenDefinitions = () =>
        _.range(2).map(() => this.Factory.collectionSpecimenDefinition());
    });
  });

  describe('for service', function() {

    it('service has correct functions', function() {
      expect(this.specimenAddModal.open).toBeFunction();
    });

    it('modal can be opened', function() {
      spyOn(this.$uibModal, 'open').and.returnValue(null);
      this.specimenAddModal.open([], []);
      expect(this.$uibModal.open).toHaveBeenCalled();
    });

  })

  describe('when modal is opened', function() {

    afterEach(function () {
      this.modalElement.remove();
      const body = this.$document.find('body');
      body.find('div.modal').remove();
      body.find('div.modal-backdrop').remove();
      body.removeClass('modal-open');
      this.$document.off('keydown');
    });

    it('has valid scope', function() {
      this.openModal();

      expect(this.scope.vm.inventoryId).toBeUndefined();
      expect(this.scope.vm.selectedSpecimenDefinition).toBeUndefined();
      expect(this.scope.vm.selectedLocationId).toBeUndefined();
      expect(this.scope.vm.amount).toBeUndefined();
      expect(this.scope.vm.defaultAmount).toBeUndefined();

      expect(this.scope.vm.centreLocations).toBeEmptyArray();
      expect(this.scope.vm.specimenDefinitions).toBeEmptyArray();
      expect(this.scope.vm.usingDefaultAmount).toBeBoolean();
      expect(this.scope.vm.timeCollected).toBeDate();
      expect(this.scope.vm.specimens).toBeEmptyArray();

      expect(this.scope.vm.okPressed).toBeFunction();
      expect(this.scope.vm.nextPressed).toBeFunction();
      expect(this.scope.vm.closePressed).toBeFunction();
      expect(this.scope.vm.specimenDefinitionChanged).toBeFunction();

      this.dismiss();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('modal is closed when okPressed is called', function () {
      var centreLocations = this.createCentreLocations(),
          specimenDefinitions = this.createSpecimenDefinitions();

      this.openModal(centreLocations, specimenDefinitions);
      this.scope.vm.selectedSpecimenDefinition = specimenDefinitions[0];
      this.scope.vm.okPressed();
      this.flush();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('when okPressed is called and a specimenDefinition is not selected, an error is thrown', function() {
      var self = this,
          centreLocations = this.createCentreLocations();

      this.openModal(centreLocations);
      this.scope.vm.selectedSpecimenDefinition = undefined;

      expect(function () {
        self.scope.vm.okPressed();
        self.flush();
      }).toThrowError('specimen type not selected');

      this.dismiss();
      expect(this.$document).toHaveModalsOpen(0);
    });

    describe('when nextPressed is called', function () {

      beforeEach(function() {
        var centreLocations = this.createCentreLocations(),
            specimenDefinitions = this.createSpecimenDefinitions();

        this.openModal(centreLocations, specimenDefinitions);
        this.scope.vm.selectedSpecimenDefinition = specimenDefinitions[0];
        this.scope.vm.selectedLocationId = centreLocations[0].locationId;
        this.scope.vm.nextPressed();
      });

      afterEach(function() {
        this.dismiss();
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('specimen variables are reset', function() {
        expect(this.scope.vm.inventoryId).toBeUndefined();
        expect(this.scope.vm.selectedSpecimenDefinition).toBeUndefined();
        expect(this.scope.vm.amount).toBeUndefined();
        expect(this.scope.vm.defaultAmount).toBeUndefined();
      });

      it('location remains unchanged', function() {
        expect(this.scope.vm.selectedLocationId).not.toBeUndefined();
      });

      it('time completed remains unchanged', function() {
        expect(this.scope.vm.selectedLocationId).not.toBeUndefined();
      });

      it('the form is reset to pristine state', function() {
        expect(this.scope.form.$setPristine).toHaveBeenCalled();
      });

    });

    it('when nextPressed is called and a specimenDefinition is not selected, an error is thrown', function() {
      var self = this,
          centreLocations = this.createCentreLocations();

      this.openModal(centreLocations);
      this.scope.vm.selectedSpecimenDefinition = undefined;

      expect(function () {
        self.scope.vm.nextPressed();
      }).toThrowError('specimen type not selected');

      this.dismiss();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('modal is closed when closedPressed is called', function() {
      this.openModal();
      this.scope.vm.closePressed();
      this.flush();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('when specimenDefinitionChanged amount, defaultAmount and units are assigned', function() {
      var centreLocations = this.createCentreLocations(),
          specimenDefinitions = this.createSpecimenDefinitions();

      specimenDefinitions[0].amount = 10;
      specimenDefinitions[1].amount = 20;

      specimenDefinitions[0].units = 'mL';
      specimenDefinitions[1].units = 'g';

      this.openModal(centreLocations, specimenDefinitions);
      this.scope.vm.selectedSpecimenDefinition = specimenDefinitions[0];
      this.scope.vm.specimenDefinitionChanged();

      expect(this.scope.vm.amount).toBe(specimenDefinitions[0].amount);
      expect(this.scope.vm.units).toBe(specimenDefinitions[0].units);

      this.scope.vm.selectedSpecimenDefinition = specimenDefinitions[1];
      this.scope.vm.specimenDefinitionChanged();

      expect(this.scope.vm.amount).toBe(specimenDefinitions[1].amount);
      expect(this.scope.vm.units).toBe(specimenDefinitions[1].units);

      this.dismiss();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('time completed is updated', function() {
      var date = faker.date.recent(10);
      this.openModal();
      this.scope.vm.dateTimeOnEdit(date);
      expect(this.scope.vm.timeCompleted).toEqual(date);
      this.dismiss();
      expect(this.$document).toHaveModalsOpen(0);
    });

    describe('for inventoryIdUpdated', function() {

      beforeEach(function() {
        this.openModal();
      });

      afterEach(function() {
        this.dismiss();
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('validity is assigned correctly for a new inventory id', function() {
        spyOn(this.Specimen, 'get').and.returnValue(this.$q.reject('simulated error'));

        this.scope.vm.specimens = [];
        this.scope.vm.inventoryId = this.Factory.stringNext();
        this.scope.vm.inventoryIdUpdated();
        this.scope.$digest();
        expect(this.scope.form.inventoryId.$setValidity.calls.allArgs())
          .toEqual([ [ 'inventoryIdEntered', true ], [ 'inventoryIdTaken', true ] ]);
      });

      it('validity is assigned correctly for a an inventory id already in the system', function() {
        var specimen = new this.Specimen.create(this.Factory.specimen());

        spyOn(this.Specimen, 'get').and.returnValue(this.$q.when(specimen));

        this.scope.vm.specimens = [];
        this.scope.vm.inventoryId = this.Factory.stringNext();
        this.scope.vm.inventoryIdUpdated();
        this.scope.$digest();
        expect(this.scope.form.inventoryId.$setValidity.calls.allArgs())
          .toEqual([
            [ 'inventoryIdEntered', true ],
            [ 'inventoryIdTaken', true ],
            [ 'inventoryIdTaken', false ]
          ]);
      });

      it('validity is assigned true', function() {
        this.scope.vm.inventoryId = undefined;
        this.scope.vm.inventoryIdUpdated();
        expect(this.scope.form.inventoryId.$setValidity.calls.allArgs())
          .toEqual([
            [ 'inventoryIdEntered', true ],
            [ 'inventoryIdTaken', true ]
          ]);
      });

    });

  })

});
