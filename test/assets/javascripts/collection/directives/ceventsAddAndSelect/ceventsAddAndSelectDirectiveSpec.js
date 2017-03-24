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

    beforeEach(inject(function(TestSuiteMixin) {
      var self = this;

      _.extend(self, TestSuiteMixin.prototype);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Participant',
                              'CollectionEvent',
                              'CollectionEventType',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/ceventsAddAndSelect/ceventsAddAndSelect.html');

      this.jsonCevent      = this.factory.collectionEvent();
      this.jsonParticipant = this.factory.defaultParticipant();
      this.jsonCeventType  = this.factory.defaultCollectionEventType();

      this.participant          = this.Participant.create(this.jsonParticipant);
      this.collectionEvent      = this.CollectionEvent.create(this.jsonCevent);
      this.pagedResult          = this.factory.pagedResult([ this.collectionEvent ]);
      this.collectionEventTypes = [ this.CollectionEventType.create(this.jsonCeventType) ];

      this.createController = function () {
        this.element = angular.element([
          '<cevents-add-and-select',
          '  participant="vm.participant"',
          '  collection-events-paged-result="vm.collectionEventsPagedResult"',
          '  collection-event-types="vm.collectionEventTypes">',
          '</cevents-add-and-select>'
        ].join(''));

        this.scope = this.$rootScope.$new();
        this.scope.vm = {
          participant:                 this.participant,
          collectionEventsPagedResult: this.pagedResult,
          collectionEventTypes:        this.collectionEventTypes
        };

        this.$compile(this.element)(this.scope);
        this.scope.$digest();
        this.controller = this.element.controller('ceventsAddAndSelect');
      };
    }));

    it('has valid scope', function() {
      this.createController();

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

        expect(function () { self.createController(); })
          .toThrowError(/no collection event types defined for this study/);
      });

      it('throws an exception when collection event does not match any collection event types', function() {
        var self = this;

        this.collectionEvent.collectionEventTypeId = self.factory.stringNext();

        expect(function () { self.createController(); })
          .toThrowError(/collection event type ID not found/);
      });

    });

    it('has valid display state when there are no collection events', function() {
      this.pagedResult = this.factory.pagedResult([]);
      this.createController();
      expect(this.controller.displayState).toBe(this.controller.displayStates.NO_RESULTS);
    });

    it('has valid display state when there are collection events', function() {
      this.createController();
      expect(this.controller.displayState).toBe(this.controller.displayStates.HAVE_RESULTS);
    });

    it('when pageChanged is called the state is changed', function() {
      spyOn(this.$state, 'go').and.returnValue('ok');
      spyOn(this.CollectionEvent, 'list').and.returnValue(this.$q.when(this.pagedResult));

      this.createController();
      this.controller.pageChanged();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.collection.study.participant.cevents');
    });

    describe('when add is called, the state is changed', function () {

      it('to correct state when there is only a single collection event type defined', function() {
        expect(this.collectionEventTypes).toBeArrayOfSize(1);

        spyOn(this.$state, 'go').and.returnValue('ok');
        spyOn(this.CollectionEvent, 'list').and.returnValue(this.$q.when(this.pagedResult));

        this.createController();
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

        self.createController();
        self.controller.add();
        self.scope.$digest();
        expect(self.$state.go).toHaveBeenCalledWith('home.collection.study.participant.cevents.add');
      });

    });

    it('when eventInformation is called the state is changed', function() {
      spyOn(this.$state, 'go').and.returnValue('ok');

      this.createController();
      this.controller.eventInformation(this.collectionEvent);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith(
        'home.collection.study.participant.cevents.details',
        { collectionEventId: this.collectionEvent.id });
    });

    describe('for updating visit number filter', function() {

      beforeEach(function() {
        spyOn(this.CollectionEvent, 'list').and.returnValue(this.$q.when(this.pagedResult));
      });


      it('filter is updated when user enters a value', function() {
        var visitNumber = '20';

        this.createController();
        this.controller.visitNumberFilter = visitNumber;
        this.controller.visitFilterUpdated();
        this.scope.$digest();

        expect(this.controller.pagerOptions.filter).toEqual('visitNumber::' + visitNumber);
        expect(this.controller.pagerOptions.page).toEqual(1);
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

});
