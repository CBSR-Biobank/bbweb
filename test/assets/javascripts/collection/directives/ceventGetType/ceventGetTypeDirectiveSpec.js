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

  describe('ceventGetTypeDirective', function() {

    function createDirective(test) {
      var element,
          scope = test.$rootScope.$new();

      element = angular.element([
        '<cevent-get-type',
        ' study="vm.study"',
        ' participant="vm.participant"',
        ' collection-event-types="vm.collectionEventTypes">',
        '</cevent-get-type>'
      ].join(''));

      scope.vm = {
        study:                test.study,
        participant:          test.participant,
        collectionEventTypes: test.collectionEventTypes
      };
      test.$compile(element)(scope);
      scope.$digest();

      return {
        element:    element,
        scope:      scope,
        controller: element.controller('ceventGetType')
      };
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(templateMixin, testUtils) {
      var self = this;

      _.extend(self, templateMixin);

      self.$q                  = self.$injector.get('$q');
      self.$rootScope          = self.$injector.get('$rootScope');
      self.$compile            = self.$injector.get('$compile');
      self.$state              = self.$injector.get('$state');
      self.Study               = self.$injector.get('Study');
      self.Participant         = self.$injector.get('Participant');
      self.CollectionEventType = self.$injector.get('CollectionEventType');
      self.jsonEntities        = self.$injector.get('jsonEntities');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/ceventGetType/ceventGetType.html');

      self.jsonCeventTypes = _.map(_.range(2), function () {
        return self.jsonEntities.collectionEventType();
      });
      self.jsonParticipant = self.jsonEntities.participant();
      self.jsonStudy       = self.jsonEntities.defaultStudy();

      self.collectionEventTypes = _.map(self.jsonCeventTypes, function (jsonCeventType){
        return new self.CollectionEventType(jsonCeventType);
      });
      self.participant = new self.Participant(self.jsonParticipant);
      self.study       = new self.Study(self.jsonStudy);

      testUtils.addCustomMatchers();

      spyOn(this.$state, 'go').and.returnValue('ok');
    }));

    it('has valid scope', function() {
      var directive = createDirective(this);

      expect(directive.controller.study).toBe(this.study);
      expect(directive.controller.participant).toBe(this.participant);
      expect(directive.controller.collectionEventTypes).toContainAll(this.collectionEventTypes);

      expect(directive.controller.title).toBeDefined();
      expect(directive.controller.collectionEvent).toBeDefined();

      expect(directive.controller.updateCollectionEventType).toBeFunction();
    });

    describe('when collection event type is updated', function() {

      it('changes to correct state selection is valid', function() {
        var directive,
            ceventTypeId = this.collectionEventTypes[0].id;

        directive = createDirective(this);
        directive.controller.collectionEvent.collectionEventTypeId = ceventTypeId;
        directive.controller.updateCollectionEventType();
        directive.scope.$digest();

        expect(this.$state.go).toHaveBeenCalledWith(
          'home.collection.study.participant.cevents.add.details',
          { collectionEventTypeId: ceventTypeId });
      });

      it('does nothing when selection is not valid', function() {
        var directive = createDirective(this);
        directive.controller.collectionEvent.collectionEventTypeId = undefined;
        directive.controller.updateCollectionEventType();
        directive.scope.$digest();

        expect(this.$state.go).not.toHaveBeenCalled();
      });

    });

  });

});
