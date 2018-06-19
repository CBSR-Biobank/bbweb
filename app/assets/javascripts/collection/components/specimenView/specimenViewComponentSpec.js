/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'

describe('specimenViewComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);
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
                              'Factory');

      this.createController =
        (study, participant, collectionEventType, collectionEvent, specimen) =>
        this.createControllerInternal(
          `<specimen-view study="vm.study"
                          participant="vm.participant"
                          collection-event="vm.collectionEvent"
                          specimen="vm.specimen">
           </specimen-view>`,
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
        var rawSpecimenDefinition = this.Factory.collectionSpecimenDefinition(),
            rawCollectionEventType = this.Factory.collectionEventType(
              { specimenDefinitions: [ rawSpecimenDefinition ]}),
            collectionEventType = this.CollectionEventType.create(rawCollectionEventType),
            collectionEvent = new this.CollectionEvent(this.Factory.collectionEvent(), collectionEventType),
            specimen = new this.Specimen(this.Factory.specimen(),
                                         collectionEventType.specimenDefinitions[0]),
            participant = new this.Participant(this.Factory.defaultParticipant()),
            study = new this.Study(this.Factory.defaultStudy());

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
    this.createController.apply(this, Object.values(entities));

    expect(this.scope.vm.study).toBe(entities.study);
    expect(this.scope.vm.participant).toBe(entities.participant);
    expect(this.scope.vm.collectionEvent).toBe(entities.collectionEvent);
    expect(this.scope.vm.specimen).toBe(entities.specimen);
  });

  it('user can return to previous page', function() {
    var entities = this.createEntities();

    this.injectDependencies('$state');
    spyOn(this.$state, 'go').and.returnValue(null);

    this.createController.apply(this, Object.values(entities));
    this.controller.back();

    expect(this.$state.go).toHaveBeenCalledWith('^');
  });

});
