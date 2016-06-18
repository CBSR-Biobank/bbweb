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

  describe('Module: specimenAddModal', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    describe('Service: specimenAddModal', function() {

      beforeEach(inject(function(testSuiteMixin) {
        _.extend(this, testSuiteMixin);
        this.injectDependencies('$uibModal', 'specimenAddModal');
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

      var createController = function (centreLocations, specimenSpecs) {
        centreLocations = centreLocations || [];
        specimenSpecs = specimenSpecs || [];
        this.scope = this.$rootScope.$new();
        this.scope.form = { $setPristine: jasmine.createSpy('modalInstance.form') };

        this.$controller('specimenAddModal.ModalInstanceController as vm', {
          $scope:            this.scope,
          $window:           this.$window,
          $timeout:          this.$timeout,
          $uibModalInstance: this.modalInstance,
          bbwebConfig:       this.bbwebConfig,
          Specimen:          this.Specimen,
          timeService:       this.timeService,
          centreLocations:   centreLocations,
          specimenSpecs:     specimenSpecs
        });
        this.scope.$digest();
      };

      var createCentreLocations = function () {
        var self = this,
            centres = _.map(_.range(2), function () {
              var locations = _.map(_.range(2), function () {
                return self.factory.location();
              });
              return self.factory.centre({ locations: locations });
            });
        return self.factory.centreLocations(centres);
      };

      var createSpecimenSpecs = function () {
        var self = this;
        return _.map(_.range(2), function () {
          return self.factory.collectionSpecimenSpec();
        });
      };

      beforeEach(inject(function(testSuiteMixin) {
        var self = this;

        _.extend(self, testSuiteMixin);

        self.injectDependencies('$q',
                                '$rootScope',
                                '$controller',
                                '$window',
                                '$timeout',
                                'bbwebConfig',
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
        createController.call(this);

        expect(this.scope.vm.inventoryId).toBeUndefined();
        expect(this.scope.vm.selectedSpecimenSpec).toBeUndefined();
        expect(this.scope.vm.selectedLocationId).toBeUndefined();
        expect(this.scope.vm.amount).toBeUndefined();
        expect(this.scope.vm.defaultAmount).toBeUndefined();

        expect(this.scope.vm.centreLocations).toBeEmptyArray();
        expect(this.scope.vm.specimenSpecs).toBeEmptyArray();
        expect(this.scope.vm.usingDefaultAmount).toBeBoolean();
        expect(this.scope.vm.timeCollected).toBeDate();
        expect(this.scope.vm.datetimePickerFormat).toBe(this.bbwebConfig.datepickerFormat);
        expect(this.scope.vm.calendarOpen).toBeFalse();
        expect(this.scope.vm.specimens).toBeEmptyArray();

        expect(this.scope.vm.okPressed).toBeFunction();
        expect(this.scope.vm.nextPressed).toBeFunction();
        expect(this.scope.vm.closePressed).toBeFunction();
        expect(this.scope.vm.openCalendar).toBeFunction();
        expect(this.scope.vm.specimenSpecChanged).toBeFunction();

      });

      it('modal is closed when okPressed is called', function () {
        var centreLocations = createCentreLocations.call(this),
            specimenSpecs = createSpecimenSpecs.call(this);

        createController.call(this, centreLocations, specimenSpecs);
        this.scope.vm.selectedSpecimenSpec = specimenSpecs[0];
        this.scope.vm.okPressed();
        expect(this.modalInstance.close).toHaveBeenCalled();
      });

      describe('when nextPressed is called', function () {

        beforeEach(function() {
          var centreLocations = createCentreLocations.call(this),
              specimenSpecs = createSpecimenSpecs.call(this);

          createController.call(this, centreLocations, specimenSpecs);
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
      });

      it('modal is closed when closedPressed is called', function() {
        createController.call(this);
        this.scope.vm.closePressed();
        expect(this.modalInstance.dismiss).toHaveBeenCalled();
      });

      it('opens the calendar when use default is unchecked', function() {
        var centreLocations = createCentreLocations.call(this),
            specimenSpecs = createSpecimenSpecs.call(this);

        createController.call(this, centreLocations, specimenSpecs);
        this.scope.vm.openCalendar();
        expect(this.scope.vm.calendarOpen).toBe(true);
      });

      it('when specimenSpecChanged amount, defaultAmount and units are assigned', function() {
        var centreLocations = createCentreLocations.call(this),
            specimenSpecs = createSpecimenSpecs.call(this);

        specimenSpecs[0].amount = 10;
        specimenSpecs[1].amount = 20;

        specimenSpecs[0].units = 'mL';
        specimenSpecs[1].units = 'g';

        createController.call(this, centreLocations, specimenSpecs);
        this.scope.vm.selectedSpecimenSpec = specimenSpecs[0];
        this.scope.vm.specimenSpecChanged();

        expect(this.scope.vm.amount).toBe(specimenSpecs[0].amount);
        expect(this.scope.vm.units).toBe(specimenSpecs[0].units);

        this.scope.vm.selectedSpecimenSpec = specimenSpecs[1];
        this.scope.vm.specimenSpecChanged();

        expect(this.scope.vm.amount).toBe(specimenSpecs[1].amount);
        expect(this.scope.vm.units).toBe(specimenSpecs[1].units);
      });

    });

  });

});
