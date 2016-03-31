/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore'
], function(angular, mocks, _) {
  'use strict';

  describe('ceventsListDirective', function() {

    function createDirective(test) {
      var element,
          scope = test.$rootScope.$new();

      element = angular.element([
        '<cevents-list',
        '  participant="vm.participant"',
        '  collection-events-paged-result="vm.collectionEventsPagedResult"',
        '  collection-event-types="vm.collectionEventTypes">',
        '</cevents-list>'
      ].join(''));

      scope.vm = {
        participant:                 test.participant,
        collectionEventsPagedResult: test.pagedResult,
        collectionEventTypes:        test.collectionEventTypes
      };
      test.$compile(element)(scope);
      scope.$digest();

      return {
        element:    element,
        scope:      scope,
        controller: element.controller('ceventsList')
      };
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(directiveTestSuite) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$q                  = self.$injector.get('$q');
      self.$rootScope          = self.$injector.get('$rootScope');
      self.$compile            = self.$injector.get('$compile');
      self.$state              = self.$injector.get('$state');
      self.Participant         = self.$injector.get('Participant');
      self.CollectionEvent     = self.$injector.get('CollectionEvent');
      self.CollectionEventType = self.$injector.get('CollectionEventType');
      self.jsonEntities        = self.$injector.get('jsonEntities');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/ceventsList/ceventsList.html',
        '/assets/javascripts/collection/directives/ceventsAddAndSelect/ceventsAddAndSelect.html');

      this.jsonCevent      = this.jsonEntities.collectionEvent();
      this.jsonParticipant = this.jsonEntities.defaultParticipant();
      this.jsonCeventType  = this.jsonEntities.defaultCollectionEventType();

      this.participant          = new this.Participant(this.jsonParticipant);
      this.collectionEvent      = new this.CollectionEvent(this.jsonCevent);
      this.pagedResult          = this.jsonEntities.pagedResult([ this.collectionEvent ]);
      this.collectionEventTypes = [ new this.CollectionEventType(this.jsonCeventType) ];
    }));

    it('has valid scope', function() {
      var directive = createDirective(this);

      expect(directive.controller.participant).toBe(this.participant);
      expect(directive.controller.collectionEventsPagedResult).toBe(this.pagedResult);
      expect(directive.controller.collectionEventTypes).toBe(this.collectionEventTypes);
    });

    it('throws an error when created with no collection event types', function() {
      var self = this;

      self.collectionEventTypes = [];
      expect(function () {
        createDirective(self);
      }).toThrowError(/no collection event types defined for this study/);
    });

  });

});
