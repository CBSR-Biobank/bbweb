/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'

describe('Component: ceventList', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$httpBackend',
                              'Participant',
                              'Factory');

      this.plainParticipant = this.Factory.defaultParticipant();
      this.participant      = this.Participant.create(this.plainParticipant);

      this.url = (...paths) => {
        const allPaths = [ 'participants/cevents' ].concat(paths);
        return ComponentTestSuiteMixin.url(...allPaths);
      }

      this.createController = (participant = this.participant) => {
        this.$httpBackend
          .whenGET(new RegExp(this.url('list')))
          .respond(this.reply(this.Factory.pagedResult([])));

        this.createControllerInternal(
          '<cevents-list participant="vm.participant"></cevents-list>',
          { participant },
          'ceventsList');
      };
    });
  });

  it('has valid scope', function() {
    this.createController();
    expect(this.controller.participant).toBe(this.participant);
    expect(this.controller.collectionEventsRefresh).toBe(0);
  });

  it('listens to the `collection-event-updated` event', function() {
    this.createController();
    const currentValue = this.controller.collectionEventsRefresh;

    const childScope = this.element.isolateScope().$new();
    childScope.$emit('collection-event-updated', null);
    this.scope.$digest();

    expect(this.controller.collectionEventsRefresh).toBe(currentValue + 1);
  });

});
