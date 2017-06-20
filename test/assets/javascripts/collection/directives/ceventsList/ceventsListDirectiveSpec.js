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

  describe('ceventsListDirective', function() {

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
        '/assets/javascripts/collection/directives/ceventsList/ceventsList.html',
        '/assets/javascripts/collection/directives/ceventsAddAndSelect/ceventsAddAndSelect.html');

      this.jsonCevent      = this.factory.collectionEvent();
      this.jsonParticipant = this.factory.defaultParticipant();
      this.jsonCeventType  = this.factory.defaultCollectionEventType();

      this.participant          = this.Participant.create(this.jsonParticipant);
      this.collectionEvent      = this.CollectionEvent.create(this.jsonCevent);
      this.pagedResult          = this.factory.pagedResult([ this.collectionEvent ]);
      this.collectionEventTypes = [ this.CollectionEventType.create(this.jsonCeventType) ];

      this.createDirective = function () {
        self.CollectionEvent.list =
          jasmine.createSpy().and.returnValue(self.$q.when(self.factory.pagedResult(this.pagedResult)));

        this.element = angular.element([
          '<cevents-list',
          '  participant="vm.participant"',
          '  collection-event-types="vm.collectionEventTypes">',
          '</cevents-list>'
        ].join(''));

        this.scope = this.$rootScope.$new();
        this.scope.vm = {
          participant:                 this.participant,
          collectionEventsPagedResult: this.pagedResult,
          collectionEventTypes:        this.collectionEventTypes
        };
        this.$compile(this.element)(this.scope);
        this.scope.$digest();
        this.controller = this.element.controller('ceventsList');
      };
    }));

    it('has valid scope', function() {
      this.createDirective();

      expect(this.controller.participant).toBe(this.participant);
      expect(this.controller.collectionEventTypes).toBe(this.collectionEventTypes);
    });

    it('throws an error when created with no collection event types', function() {
      var self = this;

      self.collectionEventTypes = [];
      expect(function () {
        self.createDirective();
      }).toThrowError(/no collection event types defined for this study/);
    });

  });

});
