/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('ceventSpecimensViewComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin.prototype);
      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Study',
                              'Specimen',
                              'CollectionEvent',
                              'CollectionEventType',
                              'specimenAddModal',
                              'domainNotificationService',
                              'factory');

      this.rawSpecimenDescription = this.factory.collectionSpecimenDescription();
      this.rawCollectionEventType = this.factory.collectionEventType(
        { specimenDescriptions: [ this.rawSpecimenDescription ]});
      this.collectionEventType = new this.CollectionEventType(this.rawCollectionEventType);
      this.collectionEvent = new this.CollectionEvent(this.factory.collectionEvent(),
                                                      this.collectionEventType);
      this.specimen = new this.Specimen(this.factory.specimen(),
                                        this.collectionEventType.specimenDescriptions[0]);
      this.study = new this.Study(this.factory.defaultStudy());

      this.createController = (study, collectionEvent) => {
        study = study || this.study;
        collectionEvent = collectionEvent || this.collectionEvent;

        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          `<cevent-specimens-view
             study="vm.study"
             collection-event="vm.collectionEvent">
           </cevent-specimens-view>`,
          {
            study:           study,
            collectionEvent: collectionEvent
          },
          'ceventSpecimensView');

        this.controller.tableController = {
          tableState: jasmine.createSpy().and.returnValue({
            sort: {
              predicate: 'inventoryId',
              reverse: false
            },
            search: {},
            pagination: { start: 0, totalItemCount: 0 }
          })
        };
      };

      this.createCentreLocations = () => {
        var centres = _.range(2).map(() => {
          var locations = _.range(2).map(() => this.factory.location());
          return this.factory.centre({ locations: locations });
        });
        return this.factory.centreLocations(centres);
      };

      spyOn(this.Specimen, 'list').and.returnValue(this.$q.when(this.factory.pagedResult([])));
      spyOn(this.Specimen, 'add').and.returnValue(this.$q.when(this.sepcimen));
      spyOn(this.specimenAddModal, 'open').and.returnValue({ result: this.$q.when([ this.specimen ])});
      spyOn(this.Study.prototype, 'allLocations')
        .and.returnValue(this.$q.when(this.createCentreLocations()));
    });
  });

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
        expect(this.Study.prototype.allLocations).toHaveBeenCalled();
        expect(this.controller.centreLocations).not.toBeEmptyArray();
      });

      it('are NOT retrieved a second time', function() {
        this.createController();
        this.controller.centreLocations = this.createCentreLocations();
        this.controller.addSpecimens();
        this.scope.$digest();
        expect(this.Study.prototype.allLocations).not.toHaveBeenCalled();
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
      this.injectDependencies('modalService', 'domainNotificationService');
      this.modalService.modalOkCancel = jasmine.createSpy().and.returnValue(this.$q.when('OK'));
      this.Specimen.prototype.remove = jasmine.createSpy().and.returnValue(this.$q.when(true));
    });

    it('opens a modal to confirm the action', function() {
      this.createController();
      this.controller.removeSpecimen(this.specimen);
      this.scope.$digest();
      expect(this.Specimen.prototype.remove).toHaveBeenCalled();
    });

    it('specimen is not removed if user cancels when asked for confirmation', function() {
      this.createController();
      this.modalService.modalOkCancel = jasmine.createSpy().and.returnValue(this.$q.reject('Cancel'));
      this.controller.removeSpecimen(this.specimen);
      this.scope.$digest();
      expect(this.Specimen.prototype.remove).not.toHaveBeenCalled();
    });

  });

  it('can view details for a single specimen', function() {
    this.injectDependencies('$state');

    spyOn(this.$state, 'go').and.returnValue(null);
    this.createController();
    this.controller.viewSpecimen(this.specimen);
    this.scope.$digest();

    expect(this.$state.go).toHaveBeenCalledWith(
      'home.collection.study.participant.cevents.details.specimen',
      { inventoryId: this.specimen.inventoryId });
  });

});
