/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';

describe('specimenViewComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin.prototype);
      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Study',
                              'Participant',
                              'CollectionEventType',
                              'CollectionEvent',
                              'Specimen',
                              'specimenAddModal',
                              'domainNotificationService',
                              'factory');

      this.createController =
        (study, participant, collectionEventType, collectionEvent, specimen) =>
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          `<specimen-view study="vm.study"
                          participant="vm.participant"
                          collection-event-type="vm.collectionEventType"
                          collection-event="vm.collectionEvent"
                          specimen="vm.specimen">
           <specimen-view>`,
          {
            study:               study,
            participant:         participant,
            collectionEventType: collectionEventType,
            collectionEvent:     collectionEvent,
            specimen:            specimen
          },
          'specimenView');

      // the object must have keys in same order as the parameters for createController()
      this.createEntities = () => {
        var rawSpecimenDescription = this.factory.collectionSpecimenDescription(),
            rawCollectionEventType = this.factory.collectionEventType(
              { specimenDescriptions: [ rawSpecimenDescription ]}),
            collectionEventType = this.CollectionEventType.create(rawCollectionEventType),
            collectionEvent = new this.CollectionEvent(this.factory.collectionEvent(), collectionEventType),
            specimen = new this.Specimen(this.factory.specimen(),
                                         collectionEventType.specimenDescriptions[0]),
            participant = new this.Participant(this.factory.defaultParticipant()),
            study = new this.Study(this.factory.defaultStudy());

        return {
          study:               study,
          participant:         participant,
          collectionEventType: collectionEventType,
          collectionEvent:     collectionEvent,
          specimen:            specimen
        };
      };
    });
  });

  it('has valid scope', function() {
    var entities = this.createEntities();
    this.createController.apply(this, _.values(entities));
    expect(this.controller.specimenDescription).toBeDefined();
  });

  it('user can return to previous page', function() {
    var entities = this.createEntities();

    this.injectDependencies('$state');
    spyOn(this.$state, 'go').and.returnValue(null);

    this.createController.apply(this, _.values(entities));
    this.controller.back();

    expect(this.$state.go).toHaveBeenCalledWith('^');
  });

});
