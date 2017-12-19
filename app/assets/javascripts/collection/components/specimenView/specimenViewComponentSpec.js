/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('specimenViewComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);
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
        ComponentTestSuiteMixin.createController.call(
          this,
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
        var rawSpecimenDescription = this.Factory.collectionSpecimenDescription(),
            rawCollectionEventType = this.Factory.collectionEventType(
              { specimenDescriptions: [ rawSpecimenDescription ]}),
            collectionEventType = this.CollectionEventType.create(rawCollectionEventType),
            collectionEvent = new this.CollectionEvent(this.Factory.collectionEvent(), collectionEventType),
            specimen = new this.Specimen(this.Factory.specimen(),
                                         collectionEventType.specimenDescriptions[0]),
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
    this.createController.apply(this, _.values(entities));
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
