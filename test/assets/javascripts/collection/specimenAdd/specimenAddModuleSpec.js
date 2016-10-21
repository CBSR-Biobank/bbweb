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

  function SuiteMixin() {
  }

  SuiteMixin.prototype.createController = function (centreLocations, specimenSpecs) {
    centreLocations = centreLocations || [];
    specimenSpecs = specimenSpecs || [];
    this.scope = this.$rootScope.$new();
    this.scope.form = {
      $setPristine: jasmine.createSpy('modalInstance.form'),
      inventoryId: { $setValidity: jasmine.createSpy('modalInstance.form.inventoryId') }
    };

    this.$controller('specimenAddModal.ModalInstanceController as vm', {
      $scope:            this.scope,
      $window:           this.$window,
      $timeout:          this.$timeout,
      $uibModalInstance: this.modalInstance,
      AppConfig:       this.AppConfig,
      Specimen:          this.Specimen,
      timeService:       this.timeService,
      centreLocations:   centreLocations,
      specimenSpecs:     specimenSpecs
    });
    this.scope.$digest();
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

  SuiteMixin.prototype.createSpecimenSpecs = function () {
    var self = this;
    return _.map(_.range(2), function () {
      return self.factory.collectionSpecimenSpec();
    });
  };


  describe('Module: specimenAddModal', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    describe('Service: specimenAddModal', function() {

      beforeEach(inject(function(TestSuiteMixin) {
        _.extend(this, TestSuiteMixin.prototype);
        this.injectDependencies('$templateCache', '$uibModal', 'specimenAddModal');
      }));

      it('service has correct functions', function() {
        expect(this.specimenAddModal.open).toBeFunction();
      });

      it('modal can be opened', function() {
        spyOn(this.$uibModal, 'open').and.returnValue(null);
        this.specimenAddModal.open([], []);
        expect(this.$uibModal.open).toHaveBeenCalled();
      });

    });

    describe('Controller: specimenAddModal modal instance', function() {

      beforeEach(inject(function(TestSuiteMixin, $templateCache) {
        var self = this;

        _.extend(self, TestSuiteMixin.prototype, SuiteMixin.prototype);

        this.putHtmlTemplates(
          '/assets/javascripts/collection/specimenAdd/specimenAdd.html',
          '/assets/javascripts/common/components/dateTimePicker/dateTimePicker.html');
        this.template = $templateCache.get('/assets/javascripts/collection/specimenAdd/specimenAdd.html');

        self.injectDependencies('$q',
                                '$rootScope',
                                '$controller',
                                '$window',
                                '$timeout',
                                '$compile',
                                'AppConfig',
                                'Specimen',
                                'timeService',
                                'factory');

        self.modalInstance = {
          close: jasmine.createSpy('modalInstance.close'),
          dismiss: jasmine.createSpy('modalInstance.dismiss'),
          result: {
            then: jasmine.createSpy('modalInstance.result.then')
          }
        };
      }));

      it('has valid scope', function() {
        this.createController();

        expect(this.scope.vm.inventoryId).toBeUndefined();
        expect(this.scope.vm.selectedSpecimenSpec).toBeUndefined();
        expect(this.scope.vm.selectedLocationId).toBeUndefined();
        expect(this.scope.vm.amount).toBeUndefined();
        expect(this.scope.vm.defaultAmount).toBeUndefined();

        expect(this.scope.vm.centreLocations).toBeEmptyArray();
        expect(this.scope.vm.specimenSpecs).toBeEmptyArray();
        expect(this.scope.vm.usingDefaultAmount).toBeBoolean();
        expect(this.scope.vm.timeCollected).toBeDate();
        expect(this.scope.vm.specimens).toBeEmptyArray();

        expect(this.scope.vm.okPressed).toBeFunction();
        expect(this.scope.vm.nextPressed).toBeFunction();
        expect(this.scope.vm.closePressed).toBeFunction();
        expect(this.scope.vm.specimenSpecChanged).toBeFunction();

      });

      it('modal is closed when okPressed is called', function () {
        var centreLocations = this.createCentreLocations(),
            specimenSpecs = this.createSpecimenSpecs();

        this.createController(centreLocations, specimenSpecs);
        this.scope.vm.selectedSpecimenSpec = specimenSpecs[0];
        this.scope.vm.okPressed();
        expect(this.modalInstance.close).toHaveBeenCalled();
      });

      it('when okPressed is called and a specimenSpec is not selected, an error is thrown', function() {
        var self = this,
            centreLocations = self.createCentreLocations();

        self.createController(centreLocations);
        self.scope.vm.selectedSpecimenSpec = undefined;

        expect(function () {
          self.scope.vm.okPressed();
        }).toThrowError('specimen type not selected');
      });

      describe('when nextPressed is called', function () {

        beforeEach(function() {
          var centreLocations = this.createCentreLocations(),
              specimenSpecs = this.createSpecimenSpecs();

          this.createController(centreLocations, specimenSpecs);
          this.scope.vm.selectedSpecimenSpec = specimenSpecs[0];
          this.scope.vm.selectedLocationId = centreLocations[0].locationId;
          this.scope.vm.nextPressed();
        });

        it('specimen variables are reset', function() {
          expect(this.scope.vm.inventoryId).toBeUndefined();
          expect(this.scope.vm.selectedSpecimenSpec).toBeUndefined();
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

        it('when nextPressed is called and a specimenSpec is not selected, an error is thrown', function() {
          var self = this,
              centreLocations = self.createCentreLocations();

          self.createController(centreLocations);
          self.scope.vm.selectedSpecimenSpec = undefined;

          expect(function () {
            self.scope.vm.nextPressed();
          }).toThrowError('specimen type not selected');
        });

      });

      it('modal is closed when closedPressed is called', function() {
        this.createController();
        this.scope.vm.closePressed();
        expect(this.modalInstance.dismiss).toHaveBeenCalled();
      });

      it('when specimenSpecChanged amount, defaultAmount and units are assigned', function() {
        var centreLocations = this.createCentreLocations(),
            specimenSpecs = this.createSpecimenSpecs();

        specimenSpecs[0].amount = 10;
        specimenSpecs[1].amount = 20;

        specimenSpecs[0].units = 'mL';
        specimenSpecs[1].units = 'g';

        this.createController(centreLocations, specimenSpecs);
        this.scope.vm.selectedSpecimenSpec = specimenSpecs[0];
        this.scope.vm.specimenSpecChanged();

        expect(this.scope.vm.amount).toBe(specimenSpecs[0].amount);
        expect(this.scope.vm.units).toBe(specimenSpecs[0].units);

        this.scope.vm.selectedSpecimenSpec = specimenSpecs[1];
        this.scope.vm.specimenSpecChanged();

        expect(this.scope.vm.amount).toBe(specimenSpecs[1].amount);
        expect(this.scope.vm.units).toBe(specimenSpecs[1].units);
      });

      it('time completed is updated', function() {
        var date = faker.date.recent(10);
        this.createController();
        this.scope.vm.dateTimeOnEdit(date);
        expect(this.scope.vm.timeCompleted).toEqual(date);
      });

      describe('for inventoryIdUpdated', function() {

        beforeEach(function() {
          this.createController();
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

});
