/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import _ from 'lodash';
import ngModule from '../../index'

describe('Component: ceventsAddAndSelect', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Participant',
                              'CollectionEvent',
                              'CollectionEventType',
                              'CollectionEventTypeName',
                              'Factory');

      this.jsonCeventType  = this.Factory.collectionEventType();
      this.jsonCevent      = this.Factory.collectionEvent();
      this.jsonParticipant = this.Factory.defaultParticipant();

      this.participant          = this.Participant.create(this.jsonParticipant);
      this.collectionEvent      = this.CollectionEvent.create(this.jsonCevent);
      this.collectionEventTypes = [ this.CollectionEventType.create(this.jsonCeventType) ];

      this.createController = (participant = this.participant, collectionEvent = this.collectionEvent) => {
        let replyItems;
        const collectionEventsRefresh = 0;

        if (_.isUndefined(collectionEvent)) {
          replyItems = [];
        } else {
          replyItems = [ collectionEvent ];
        }

        this.CollectionEvent.list =
          jasmine.createSpy().and.returnValue(this.$q.when(this.Factory.pagedResult(replyItems)));

        this.createControllerInternal(
          `<cevents-add-and-select
              participant="vm.participant"
              collection-events-refresh="vm.updateValue">
           </cevents-add-and-select>`,
          {
            participant:             participant,
            collectionEventsRefresh: collectionEventsRefresh
          },
          'ceventsAddAndSelect');
      };
    });
  });

  it('has valid scope', function() {
    this.createController();
    expect(this.controller.participant).toBe(this.participant);

    expect(this.controller.pageChanged).toBeFunction();
    expect(this.controller.add).toBeFunction();
    expect(this.controller.eventInformation).toBeFunction();
    expect(this.controller.displayState).toBe(1 /* HAVE_RESULS */);
  });

  it('has valid display state when there are no collection events', function() {
    this.collectionEvent = undefined;
    this.createController(this.participant, undefined);
    expect(this.controller.displayState).toBe(2 /* NONE_ADDED */);
  });

  it('has valid display state when there are collection events', function() {
    this.createController();
    expect(this.controller.displayState).toBe(1 /* HAVE_RESULTS */);
  });

  it('when pageChanged is called the state is changed', function() {
    spyOn(this.$state, 'go').and.returnValue('ok');
    this.createController();
    this.controller.pageChanged();
    this.scope.$digest();
    expect(this.$state.go).toHaveBeenCalledWith('home.collection.study.participant.cevents');
  });

  describe('when add is called, the state is changed', function () {

    it('to correct state when there is only a single collection event type defined', function() {
      const typeName = this.Factory.collectionEventTypeNameDto();
      this.collectionEventTypes = [ this.Factory.defaultCollectionEventType() ];

      spyOn(this.$state, 'go').and.returnValue('ok');
      this.CollectionEventTypeName.list = jasmine.createSpy().and.returnValue(this.$q.when([ typeName ]));

      this.createController();
      this.controller.add();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.collection.study.participant.cevents.add.details',
                                                  { eventTypeSlug: this.collectionEventTypes[0].slug });
    });

    it('to correct state when there is more than one collection event type defined', function() {
      const typeNames = [];
      this.collectionEventTypes = _.range(2).map(() => {
        typeNames.push(this.Factory.collectionEventTypeNameDto());
        return this.CollectionEventType.create(this.Factory.defaultCollectionEventType());
      });
      spyOn(this.$state, 'go').and.returnValue('ok');
      this.createController();

      this.CollectionEventTypeName.list = jasmine.createSpy().and.returnValue(this.$q.when(typeNames));
      this.controller.add();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.collection.study.participant.cevents.add');
    });

  });

  it('when eventInformation is called the state is changed', function() {
    spyOn(this.$state, 'go').and.returnValue('ok');

    this.createController();
    this.controller.eventInformation(this.collectionEvent);
    this.scope.$digest();
    expect(this.$state.go).toHaveBeenCalledWith(
      'home.collection.study.participant.cevents.details', {
        visitNumber: this.collectionEvent.visitNumber
      });
  });

  it('events are reloaded when `collectionEventsRefresh` is changed', function() {
    this.createController();
    this.CollectionEvent.list = jasmine.createSpy().and.returnValue(this.$q.when([ this.collectionEvent]))
    this.scope.vm.collectionEventsRefresh += 1;
    this.controller.$onChanges({ collectionEventsRefresh: true });
    expect(this.CollectionEvent.list).toHaveBeenCalled();
  });

  describe('for updating visit number filter', function() {

    it('filter is updated when user enters a value', function() {
      var visitNumber = '20';
      this.createController();

      this.CollectionEvent.list =
        jasmine.createSpy().and.returnValue(this.$q.when(this.Factory.pagedResult([])));

      this.controller.visitNumberFilter = visitNumber;
      this.controller.visitFilterUpdated();
      this.scope.$digest();

      expect(this.controller.pagerOptions.filter).toEqual('visitNumber::' + visitNumber);
      expect(this.controller.pagerOptions.page).toEqual(1);
      expect(this.controller.displayState).toBe(0 /* NO_RESULTS */);
    });

    it('filter is updated when user clears the value', function() {
      this.createController();
      this.controller.visitNumberFilter = '';
      this.controller.visitFilterUpdated();
      this.scope.$digest();

      expect(this.controller.pagerOptions.filter).toBeEmptyString();
      expect(this.controller.pagerOptions.page).toEqual(1);
    });

  });

});
