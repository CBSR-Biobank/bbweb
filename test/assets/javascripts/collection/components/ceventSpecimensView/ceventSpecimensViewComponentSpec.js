/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  describe('ceventSpecimensViewComponent', function() {

    var createController = function (study, collectionEvent) {
      study = study || this.study;
      collectionEvent = collectionEvent || this.collectionEvent;
      this.scope = this.$rootScope.$new();
      this.controller = this.$componentController('ceventSpecimensView',
                                                  null,
                                                  { study:           study,
                                                    collectionEvent: collectionEvent });

      this.controller.tableController = {
        tableState: jasmine.createSpy().and.returnValue({
          sort: { predicate: 'inventoryId', reverse: false },
          search: {},
          pagination: { start: 0, totalItemCount: 0 }
        })
      };
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

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$componentController',
                              'Study',
                              'Specimen',
                              'CollectionEvent',
                              'CollectionEventType',
                              'specimenAddModal',
                              'domainEntityService',
                              'factory');

      self.rawSpecimenSpec = self.factory.collectionSpecimenSpec();
      self.rawCollectionEventType = self.factory.collectionEventType(
        { specimenSpecs: [ self.rawSpecimenSpec ]});
      self.collectionEventType = new self.CollectionEventType(self.rawCollectionEventType);
      self.collectionEvent = new self.CollectionEvent(self.factory.collectionEvent(),
                                                      self.collectionEventType);
      self.specimen = new self.Specimen(self.factory.specimen(),
                                        self.collectionEventType.specimenSpecs[0]);
      self.study = new self.Study(self.factory.defaultStudy());

      spyOn(self.Specimen, 'list').and.returnValue(self.$q.when(self.factory.pagedResult([])));
      spyOn(self.Specimen, 'add').and.returnValue(self.$q.when(self.sepcimen));
      spyOn(self.specimenAddModal, 'open').and.returnValue({ result: self.$q.when([ self.specimen ])});
      spyOn(self.Study.prototype, 'allLocations')
        .and.returnValue(self.$q.when(createCentreLocations.call(self)));
    }));

    it('has valid scope', function() {
      createController.call(this);

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
          createController.call(this);
          expect(this.controller.centreLocations).toBeEmptyArray();
          this.controller.addSpecimens();
          this.scope.$digest();
          expect(this.Study.prototype.allLocations).toHaveBeenCalled();
          expect(this.controller.centreLocations).not.toBeEmptyArray();
        });

        it('are NOT retrieved a second time', function() {
          createController.call(this);
          this.controller.centreLocations = createCentreLocations.call(this);
          this.controller.addSpecimens();
          this.scope.$digest();
          expect(this.Study.prototype.allLocations).not.toHaveBeenCalled();
        });

      });

      it('can add a specimen', function() {
        createController.call(this);
        this.controller.centreLocations = createCentreLocations.call(this);
        this.controller.addSpecimens();
        this.scope.$digest();
        expect(this.Specimen.add).toHaveBeenCalledWith(
          this.collectionEvent.id, [ this.specimen ]);
      });

    });

    describe('when removing a specimen', function() {

      beforeEach(function() {
        this.injectDependencies('modalService', 'domainEntityService');
        this.modalService.showModal = jasmine.createSpy().and.returnValue(this.$q.when('OK'));
        this.Specimen.prototype.remove = jasmine.createSpy().and.returnValue(this.$q.when(true));
      });

      it('opens a modal to confirm the action', function() {
        createController.call(this);
        this.controller.removeSpecimen(this.specimen);
        this.scope.$digest();
        expect(this.Specimen.prototype.remove).toHaveBeenCalled();
      });

      it('specimen is not removed if user cancels when asked for confirmation', function() {
        var deferred = this.$q.defer();
        deferred.reject('CANCEL');

        this.modalService.showModal = jasmine.createSpy().and.returnValue(deferred.promise);

        createController.call(this);
        this.controller.removeSpecimen(this.specimen);
        this.scope.$digest();
        expect(this.Specimen.prototype.remove).not.toHaveBeenCalled();
      });


    });

  });

});
