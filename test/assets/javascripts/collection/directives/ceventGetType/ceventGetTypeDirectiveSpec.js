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

  describe('ceventGetTypeDirective', function() {

    var createDirective = function () {
      this.element = angular.element([
        '<cevent-get-type',
        '  study="vm.study"',
        '  participant="vm.participant"',
        '  collection-event-types="vm.collectionEventTypes">',
        '</cevent-get-type>'
      ].join(''));

      this.scope = this.$rootScope.$new();
      this.scope.vm = {
        study:                this.study,
        participant:          this.participant,
        collectionEventTypes: this.collectionEventTypes
      };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('ceventGetType');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin, testUtils) {
      var self = this;

      _.extend(self, TestSuiteMixin.prototype);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Study',
                              'Participant',
                              'CollectionEventType',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/ceventGetType/ceventGetType.html');

      self.jsonCeventTypes = _.map(_.range(2), function () {
        return self.factory.collectionEventType();
      });
      self.jsonParticipant = self.factory.participant();
      self.jsonStudy       = self.factory.defaultStudy();

      self.collectionEventTypes = _.map(self.jsonCeventTypes, function (jsonCeventType){
        return new self.CollectionEventType(jsonCeventType);
      });
      self.participant = new self.Participant(self.jsonParticipant);
      self.study       = new self.Study(self.jsonStudy);

      testUtils.addCustomMatchers();

      spyOn(this.$state, 'go').and.returnValue(null);
    }));

    it('has valid scope', function() {
      createDirective.call(this);

      expect(this.controller.study).toBe(this.study);
      expect(this.controller.participant).toBe(this.participant);
      expect(this.controller.collectionEventTypes).toContainAll(this.collectionEventTypes);

      expect(this.controller.title).toBeDefined();
      expect(this.controller.collectionEvent).toBeDefined();

      expect(this.controller.updateCollectionEventType).toBeFunction();
    });

    describe('when collection event type is updated', function() {

      it('changes to correct state selection is valid', function() {
        var ceventTypeId = this.collectionEventTypes[0].id;

        createDirective.call(this);
        this.controller.collectionEvent.collectionEventTypeId = ceventTypeId;
        this.controller.updateCollectionEventType();
        this.scope.$digest();

        expect(this.$state.go).toHaveBeenCalledWith(
          'home.collection.study.participant.cevents.add.details',
          { collectionEventTypeId: ceventTypeId });
      });

      it('does nothing when selection is not valid', function() {
        createDirective.call(this);
        this.controller.collectionEvent.collectionEventTypeId = undefined;
        this.controller.updateCollectionEventType();
        this.scope.$digest();

        expect(this.$state.go).not.toHaveBeenCalled();
      });

    });

  });

});
