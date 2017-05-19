/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'faker'
], function(angular, mocks, _, faker) {
  'use strict';

  function SuiteMixinFactory(ModalTestSuiteMixin) {

    function SuiteMixin() {
      ModalTestSuiteMixin.call(this);
    }

    SuiteMixin.prototype = Object.create(ModalTestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    SuiteMixin.prototype.openModal = function (centreLocations, specimenDescriptions, defaultDatetime) {
      centreLocations = centreLocations || [];
      specimenDescriptions = specimenDescriptions || [];
      defaultDatetime = defaultDatetime || new Date();
      this.modal = this.specimenAddModal.open(centreLocations, specimenDescriptions, defaultDatetime);
      this.$rootScope.$digest();
      this.modalElement = this.modalElementFind();
      this.scope = this.modalElement.scope();

      this.scope.form = {
        $setPristine: jasmine.createSpy('modalInstance.form'),
        inventoryId: { $setValidity: jasmine.createSpy('modalInstance.form.inventoryId') }
      };
    };

    SuiteMixin.prototype.createCentreLocations = function () {
      var self = this,
          centres = _.map(_.range(2), function () {
            var locations = _.map(_.range(2), function () {
              return self.factory.location();
            });
            return self.factory.centre({ locations: locations });
          });
      return self.factory.centreLocations(centres);
    };

    SuiteMixin.prototype.createSpecimenDescriptions = function () {
      var self = this;
      return _.map(_.range(2), function () {
        return self.factory.collectionSpecimenDescription();
      });
    };

    return SuiteMixin;
  }

  describe('Service: specimenAddService', function() {

    beforeEach(mocks.module('ngAnimateMock', 'biobankApp', 'biobank.test'));

    beforeEach(inject(function(ModalTestSuiteMixin) {
      var SuiteMixin = new SuiteMixinFactory(ModalTestSuiteMixin);
      _.extend(this, SuiteMixin.prototype);

      this.putHtmlTemplates(
        '/assets/javascripts/collection/services/specimenAddModal/specimenAddModal.html',
        '/assets/javascripts/common/components/dateTimePicker/dateTimePicker.html');

      this.injectDependencies('$uibModal',
                              '$q',
                              '$rootScope',
                              '$document',
                              'specimenAddModal',
                              'AppConfig',
                              'Specimen',
                              'timeService',
                              'factory');
      this.addModalMatchers();
    }));

    it('service has correct functions', function() {
      expect(this.specimenAddModal.open).toBeFunction();
    });

    it('modal can be opened', function() {
      spyOn(this.$uibModal, 'open').and.returnValue(null);
      this.specimenAddModal.open([], []);
      expect(this.$uibModal.open).toHaveBeenCalled();
    });

    it('has valid scope', function() {
      this.openModal();

      expect(this.scope.vm.inventoryId).toBeUndefined();
      expect(this.scope.vm.selectedSpecimenDescription).toBeUndefined();
      expect(this.scope.vm.selectedLocationId).toBeUndefined();
      expect(this.scope.vm.amount).toBeUndefined();
      expect(this.scope.vm.defaultAmount).toBeUndefined();

      expect(this.scope.vm.centreLocations).toBeEmptyArray();
      expect(this.scope.vm.specimenDescriptions).toBeEmptyArray();
      expect(this.scope.vm.usingDefaultAmount).toBeBoolean();
      expect(this.scope.vm.timeCollected).toBeDate();
      expect(this.scope.vm.specimens).toBeEmptyArray();

      expect(this.scope.vm.okPressed).toBeFunction();
      expect(this.scope.vm.nextPressed).toBeFunction();
      expect(this.scope.vm.closePressed).toBeFunction();
      expect(this.scope.vm.specimenDescriptionChanged).toBeFunction();

      this.dismiss();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('modal is closed when okPressed is called', function () {
      var centreLocations = this.createCentreLocations(),
      specimenDescriptions = this.createSpecimenDescriptions();

      this.openModal(centreLocations, specimenDescriptions);
      this.scope.vm.selectedSpecimenDescription = specimenDescriptions[0];
      this.scope.vm.okPressed();
      this.flush();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('when okPressed is called and a specimenDescription is not selected, an error is thrown', function() {
      var self = this,
      centreLocations = self.createCentreLocations();

      self.openModal(centreLocations);
      self.scope.vm.selectedSpecimenDescription = undefined;

      expect(function () {
        self.scope.vm.okPressed();
        this.flush();
      }).toThrowError('specimen type not selected');

      self.dismiss();
      expect(self.$document).toHaveModalsOpen(0);
    });

    describe('when nextPressed is called', function () {

      beforeEach(function() {
        var centreLocations = this.createCentreLocations(),
            specimenDescriptions = this.createSpecimenDescriptions();

        this.openModal(centreLocations, specimenDescriptions);
        this.scope.vm.selectedSpecimenDescription = specimenDescriptions[0];
        this.scope.vm.selectedLocationId = centreLocations[0].locationId;
        this.scope.vm.nextPressed();
      });

      afterEach(function() {
        this.dismiss();
        expect(this.$document).toHaveModalsOpen(0);
      });

      it('specimen variables are reset', function() {
        expect(this.scope.vm.inventoryId).toBeUndefined();
        expect(this.scope.vm.selectedSpecimenDescription).toBeUndefined();
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

    it('when nextPressed is called and a specimenDescription is not selected, an error is thrown', function() {
      var self = this,
          centreLocations = self.createCentreLocations();

      self.openModal(centreLocations);
      self.scope.vm.selectedSpecimenDescription = undefined;

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

    it('when specimenDescriptionChanged amount, defaultAmount and units are assigned', function() {
      var centreLocations = this.createCentreLocations(),
      specimenDescriptions = this.createSpecimenDescriptions();

      specimenDescriptions[0].amount = 10;
      specimenDescriptions[1].amount = 20;

      specimenDescriptions[0].units = 'mL';
      specimenDescriptions[1].units = 'g';

      this.openModal(centreLocations, specimenDescriptions);
      this.scope.vm.selectedSpecimenDescription = specimenDescriptions[0];
      this.scope.vm.specimenDescriptionChanged();

      expect(this.scope.vm.amount).toBe(specimenDescriptions[0].amount);
      expect(this.scope.vm.units).toBe(specimenDescriptions[0].units);

      this.scope.vm.selectedSpecimenDescription = specimenDescriptions[1];
      this.scope.vm.specimenDescriptionChanged();

      expect(this.scope.vm.amount).toBe(specimenDescriptions[1].amount);
      expect(this.scope.vm.units).toBe(specimenDescriptions[1].units);

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
        this.Specimen.getByInventoryId =
          jasmine.createSpy('getByInventoryId').and.returnValue(this.$q.reject('simulated error'));

        this.scope.vm.specimens = [];
        this.scope.vm.inventoryId = this.factory.stringNext();
        this.scope.vm.inventoryIdUpdated();
        this.scope.$digest();
        expect(this.scope.form.inventoryId.$setValidity.calls.allArgs())
          .toEqual([ [ 'inventoryIdEntered', true ], [ 'inventoryIdTaken', true ] ]);
      });

      it('validity is assigned correctly for a an inventory id already in the system', function() {
        var specimen = new this.Specimen(this.factory.specimen());

        this.Specimen.getByInventoryId =
          jasmine.createSpy('getByInventoryId').and.returnValue(this.$q.when(specimen));

        this.scope.vm.specimens = [];
        this.scope.vm.inventoryId = this.factory.stringNext();
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
          .toEqual([ [ 'inventoryIdEntered', true ], [ 'inventoryIdTaken', true ] ]);
      });

    });

  });

});
