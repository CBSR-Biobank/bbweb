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

  describe('ceventsAddAndSelectDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(templateMixin) {
      var self = this;

      _.extend(self, templateMixin);

      self.$q                  = self.$injector.get('$q');
      self.$rootScope          = self.$injector.get('$rootScope');
      self.$compile            = self.$injector.get('$compile');
      self.$state              = self.$injector.get('$state');
      self.Participant         = self.$injector.get('Participant');
      self.CollectionEvent     = self.$injector.get('CollectionEvent');
      self.CollectionEventType = self.$injector.get('CollectionEventType');
      self.factory        = self.$injector.get('factory');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/ceventsAddAndSelect/ceventsAddAndSelect.html');

      this.jsonCevent      = this.factory.collectionEvent();
      this.jsonParticipant = this.factory.defaultParticipant();
      this.jsonCeventType  = this.factory.defaultCollectionEventType();

      this.participant          = new this.Participant(this.jsonParticipant);
      this.collectionEvent      = new this.CollectionEvent(this.jsonCevent);
      this.pagedResult          = this.factory.pagedResult([ this.collectionEvent ]);
      this.collectionEventTypes = [ new this.CollectionEventType(this.jsonCeventType) ];
    }));

    function createController(test) {
      test.element = angular.element([
        '<cevents-add-and-select',
        '  participant="vm.participant"',
        '  collection-events-paged-result="vm.collectionEventsPagedResult"',
        '  collection-event-types="vm.collectionEventTypes">',
        '</cevents-add-and-select>'
      ].join(''));

      test.scope = test.$rootScope.$new();
      test.scope.vm = {
        participant:                 test.participant,
        collectionEventsPagedResult: test.pagedResult,
        collectionEventTypes:        test.collectionEventTypes
      };

      test.$compile(test.element)(test.scope);
      test.scope.$digest();
      test.controller = test.element.controller('ceventsAddAndSelect');
    }

    it('has valid scope', function() {
      createController(this);

      expect(this.controller.participant).toBe(this.participant);
      expect(this.controller.collectionEventsPagedResult).toBe(this.pagedResult);
      expect(this.controller.collectionEventTypes).toBe(this.collectionEventTypes);

      expect(this.controller.pageChanged).toBeFunction();
      expect(this.controller.add).toBeFunction();
      expect(this.controller.eventInformation).toBeFunction();
    });

    describe('creating controller', function () {

      it('throws an exception when no collection event types are avaiable', function() {
        var self = this;

        self.collectionEventTypes = [];

        expect(function () { createController(self); })
          .toThrowError(/no collection event types defined for this study/);
      });

      it('throws an exception when collection event does not match any collection event types', function() {
        var self = this;

        this.collectionEvent.collectionEventTypeId = self.factory.stringNext();

        expect(function () { createController(self); })
          .toThrowError(/collection event type ID not found/);
      });

    });

    it('has valid display state when there are no collection events', function() {
      this.pagedResult = this.factory.pagedResult([]);
      createController(this);
      expect(this.controller.displayState).toBe(this.controller.displayStates.NO_RESULTS);
    });

    it('has valid display state when there are collection events', function() {
      createController(this);
      expect(this.controller.displayState).toBe(this.controller.displayStates.HAVE_RESULTS);
    });

    it('when pageChanged is called the state is changed', function() {
      spyOn(this.$state, 'go').and.returnValue('ok');
      spyOn(this.CollectionEvent, 'list').and.returnValue(this.$q.when(this.pagedResult));

      createController(this);
      this.controller.pageChanged();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.collection.study.participant.cevents');
    });

    describe('when add is called, the state is changed', function () {

      it('to correct state when there is only a single collection event type defined', function() {
        expect(this.collectionEventTypes).toBeArrayOfSize(1);

        spyOn(this.$state, 'go').and.returnValue('ok');
        spyOn(this.CollectionEvent, 'list').and.returnValue(this.$q.when(this.pagedResult));

        createController(this);
        this.controller.add();
        this.scope.$digest();
        expect(this.$state.go).toHaveBeenCalledWith(
          'home.collection.study.participant.cevents.add.details',
          { collectionEventTypeId: this.collectionEventTypes[0].id });
      });

      it('to correct state when there is more than one collection event type defined', function() {
        var self = this;

        self.collectionEventTypes = _.map(_.range(2), function () {
          var jsonCeventType  = self.factory.defaultCollectionEventType();
          return new self.CollectionEventType(jsonCeventType);
        });

        spyOn(self.$state, 'go').and.returnValue('ok');
        spyOn(self.CollectionEvent, 'list').and.returnValue(self.$q.when(self.pagedResult));

        createController(self);
        self.controller.add();
        self.scope.$digest();
        expect(self.$state.go).toHaveBeenCalledWith('home.collection.study.participant.cevents.add');
      });

    });

    it('when eventInformation is called the state is changed', function() {
      spyOn(this.$state, 'go').and.returnValue('ok');

      createController(this);
      this.controller.eventInformation(this.collectionEvent);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith(
        'home.collection.study.participant.cevents.details',
        { collectionEventId: this.collectionEvent.id });
    });

  });

});
