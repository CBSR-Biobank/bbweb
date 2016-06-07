/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore'
], function(angular, mocks, _) {
  'use strict';

  describe('ceventSpecimensViewComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $componentController) {
      var self = this;

      self.$q                  = self.$injector.get('$q');
      self.Centre              = self.$injector.get('Centre');
      self.Specimen            = self.$injector.get('Specimen');
      self.CollectionEvent     = self.$injector.get('CollectionEvent');
      self.CollectionEventType = self.$injector.get('CollectionEventType');
      self.specimenAddModal    = self.$injector.get('specimenAddModal');
      self.domainEntityService = self.$injector.get('domainEntityService');
      self.factory             = self.$injector.get('factory');

      self.rawSpecimenSpec = self.factory.collectionSpecimenSpec();
      self.rawCollectionEventType = self.factory.collectionEventType(
        { specimenSpecs: [ self.rawSpecimenSpec ]});
      self.collectionEventType = new self.CollectionEventType(self.rawCollectionEventType);
      self.collectionEvent = new self.CollectionEvent(self.factory.collectionEvent(),
                                                      self.collectionEventType);
      self.specimen = new self.Specimen(self.factory.specimen(),
                                        self.collectionEventType.specimenSpecs[0]);

      self.createController = createController;
      self.createCentreLocations = createCentreLocations;

      spyOn(self.Specimen, 'list').and.returnValue(self.$q.when(self.factory.pagedResult([])));
      spyOn(self.Specimen, 'add').and.returnValue(self.$q.when(self.sepcimen));
      spyOn(self.specimenAddModal, 'open').and.returnValue({ result: self.$q.when([ self.specimen ])});
      spyOn(self.Centre, 'allLocations').and.returnValue(self.$q.when(createCentreLocations()));

      //---

      function createController(collectionEvent) {
        collectionEvent = collectionEvent || self.collectionEvent;
        self.scope = $rootScope.$new();
        self.controller = $componentController('ceventSpecimensView',
                                               null,
                                               { collectionEvent: collectionEvent });

        self.controller.tableController = {
          tableState: jasmine.createSpy().and.returnValue({
            sort: { predicate: 'inventoryId', reverse: false },
            search: {},
            pagination: { start: 0, totalItemCount: 0 }
          })
        };
      }

      function createCentreLocations() {
        var centres = _.map(_.range(2), function () {
          var locations = _.map(_.range(2), function () {
            return self.factory.location();
          });
          return self.factory.centre({ locations: locations });
        });
        return self.factory.centreLocations(centres);
      }
    }));

    it('has valid scope', function() {
      this.createController();

      expect(this.controller.specimens).toBeEmptyArray();
      expect(this.controller.centreLocations).toBeEmptyArray();
      expect(this.controller.tableController).toBeDefined(); // defined by the test suite

      expect(this.controller.addSpecimens).toBeFunction();
      expect(this.controller.getTableData).toBeFunction();
      expect(this.controller.removeSpecimen).toBeFunction();
    });

    describe('when adding a specimen', function() {

      describe('centre locations', function() {

        it('are retrieved the first time addSpecimens is called', function() {
          this.createController();
          expect(this.controller.centreLocations).toBeEmptyArray();
          this.controller.addSpecimens();
          this.scope.$digest();
          expect(this.Centre.allLocations).toHaveBeenCalled();
          expect(this.controller.centreLocations).not.toBeEmptyArray();
        });

        it('are NOT retrieved a second time', function() {
          this.createController();
          this.controller.centreLocations = this.createCentreLocations();
          this.controller.addSpecimens();
          this.scope.$digest();
          expect(this.Centre.allLocations).not.toHaveBeenCalled();
        });

      });

      it('can add a specimen', function() {
        this.createController();
        this.controller.centreLocations = this.createCentreLocations();
        this.controller.addSpecimens();
        this.scope.$digest();
        expect(this.Specimen.add).toHaveBeenCalledWith(
          this.collectionEvent.id, [ this.specimen ]);
      });

    });

    describe('when removing a specimen', function() {

      beforeEach(function() {
        this.modalService = this.$injector.get('modalService');
        this.domainEntityService = this.$injector.get('domainEntityService');
        this.modalService.showModal = jasmine.createSpy().and.returnValue(this.$q.when('OK'));
        this.Specimen.prototype.remove = jasmine.createSpy().and.returnValue(this.$q.when(true));
      });

      it('opens a modal to confirm the action', function() {
        this.createController();
        this.controller.removeSpecimen(this.specimen);
        this.scope.$digest();
        expect(this.Specimen.prototype.remove).toHaveBeenCalled();
      });

      it('specimen is not removed if user cancels when asked for confirmation', function() {
        var deferred = this.$q.defer();
        deferred.reject('CANCEL');

        this.modalService.showModal = jasmine.createSpy().and.returnValue(deferred.promise);

        this.createController();
        this.controller.removeSpecimen(this.specimen);
        this.scope.$digest();
        expect(this.Specimen.prototype.remove).not.toHaveBeenCalled();
      });


    });

  });

});
