/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Component: ceventList', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin.prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Participant',
                              'CollectionEvent',
                              'CollectionEventType',
                              'factory');

      this.jsonCevent      = this.factory.collectionEvent();
      this.jsonCeventType  = this.factory.defaultCollectionEventType();
      this.jsonParticipant = this.factory.defaultParticipant();

      this.participant          = new this.Participant(this.jsonParticipant);
      this.collectionEventTypes = [ new this.CollectionEventType(this.jsonCeventType) ];
      this.collectionEvent      = new this.CollectionEvent(this.jsonCevent);

      this.CollectionEvent.list = jasmine.createSpy()
        .and.returnValue(this.$q.when(this.factory.pagedResult([ this.collectionEvent ])));

      this.createController = (participant, collectionEventTypes) => {
        participant = participant || this.participant;
        collectionEventTypes = collectionEventTypes || this.collectionEventTypes;

        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          `<cevents-list
             participant="vm.participant"
             collection-event-types="vm.collectionEventTypes">
           </cevents-list>`,
          {
            participant:          participant,
            collectionEventTypes: collectionEventTypes
          },
          'ceventsList');
      };
    });
  });

  it('has valid scope', function() {
    this.createController();

    expect(this.controller.participant).toBe(this.participant);
    expect(this.controller.collectionEventTypes).toBe(this.collectionEventTypes);
  });

  it('component creattion throws an error if there are no collection event types', function() {
    var self = this;

    expect(function () {
      self.createController(self.participant, []);
    }).toThrowError(/no collection event types defined for this study/);
  });

});
