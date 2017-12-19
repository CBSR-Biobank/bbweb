/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Component: ceventList', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Participant',
                              'CollectionEvent',
                              'CollectionEventType',
                              'Factory');

      this.jsonCevent      = this.Factory.collectionEvent();
      this.jsonCeventType  = this.Factory.defaultCollectionEventType();
      this.jsonParticipant = this.Factory.defaultParticipant();

      this.participant          = this.Participant.create(this.jsonParticipant);
      this.collectionEventTypes = [ new this.CollectionEventType(this.jsonCeventType) ];
      this.collectionEvent      = this.CollectionEvent.create(this.jsonCevent);

      this.CollectionEventType.list = jasmine.createSpy()
        .and.returnValue(this.$q.when(this.Factory.pagedResult(this.collectionEventTypes)));

      this.CollectionEvent.list = jasmine.createSpy()
        .and.returnValue(this.$q.when(this.Factory.pagedResult([ this.collectionEvent ])));

      this.createController = (participant = this.participant) => {
        ComponentTestSuiteMixin.createController.call(
          this,
          `<cevents-list participant="vm.participant" update-collection-events="vm.updateValue">
           </cevents-list>`,
          {
            participant: participant,
            updateValue: this.updateValue
          },
          'ceventsList');
      };
    });
  });

  it('has valid scope', function() {
    this.createController();
    expect(this.controller.participant).toBe(this.participant);
  });

});
