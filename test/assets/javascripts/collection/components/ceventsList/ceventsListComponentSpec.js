/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('Component: ceventList', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (participant, collectionEventTypes) {
        participant = participant || this.participant;
        collectionEventTypes = collectionEventTypes || this.collectionEventTypes;

        ComponentTestSuiteMixin.prototype.createScope.call(
          this,
          [
            '<cevents-list',
            '  participant="vm.participant"',
            '  collection-event-types="vm.collectionEventTypes">',
            '</cevents-list>'
          ].join(''),
          {
            participant:          participant,
            collectionEventTypes: collectionEventTypes
          },
          'ceventsList');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Participant',
                              'CollectionEvent',
                              'CollectionEventType',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/collection/components/ceventsList/ceventsList.html',
        '/assets/javascripts/collection/components/ceventsAddAndSelect/ceventsAddAndSelect.html');

      this.jsonCevent      = this.factory.collectionEvent();
      this.jsonCeventType  = this.factory.defaultCollectionEventType();
      this.jsonParticipant = this.factory.defaultParticipant();

      this.participant          = new this.Participant(this.jsonParticipant);
      this.collectionEventTypes = [ new this.CollectionEventType(this.jsonCeventType) ];
      this.collectionEvent      = new this.CollectionEvent(this.jsonCevent);

      this.CollectionEvent.list = jasmine.createSpy()
        .and.returnValue(this.$q.when(this.factory.pagedResult([ this.collectionEvent ])));
    }));

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

});
