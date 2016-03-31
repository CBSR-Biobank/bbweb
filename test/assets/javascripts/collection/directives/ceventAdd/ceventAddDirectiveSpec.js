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

  describe('ceventAddDirective', function() {

    function createDirective(test) {
      var element,
          scope = test.$rootScope.$new();

      element = angular.element([
        '<cevent-add',
        '  study="vm.study"',
        '  participant="vm.participant"',
        '  collection-event-type="vm.collectionEventType">',
        '</cevent-add>'
      ].join(''));

      scope.vm = {
        study:               test.study,
        participant:         test.participant,
        collectionEventType: test.collectionEventType
      };
      test.$compile(element)(scope);
      scope.$digest();

      return {
        element:    element,
        scope:      scope,
        controller: element.controller('ceventAdd')
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
      self.domainEntityService = self.$injector.get('domainEntityService');
      self.Study               = self.$injector.get('Study');
      self.Participant         = self.$injector.get('Participant');
      self.CollectionEvent     = self.$injector.get('CollectionEvent');
      self.CollectionEventType = self.$injector.get('CollectionEventType');
      self.jsonEntities        = self.$injector.get('jsonEntities');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/ceventAdd/ceventAdd.html',
        '/assets/javascripts/common/directives/dateTime/dateTime.html',
        '/assets/javascripts/common/annotationsInput/annotationsInput.html',
        '/assets/javascripts/common/annotationsInput/textAnnotation.html',
        '/assets/javascripts/common/annotationsInput/numberAnnotation.html',
        '/assets/javascripts/common/annotationsInput/dateTimeAnnotation.html',
        '/assets/javascripts/common/annotationsInput/singleSelectAnnotation.html',
        '/assets/javascripts/common/annotationsInput/multipleSelectAnnotation.html');

      self.jsonCevent      = self.jsonEntities.collectionEvent();
      self.jsonCeventType  = self.jsonEntities.defaultCollectionEventType();
      self.jsonParticipant = self.jsonEntities.defaultParticipant();
      self.jsonStudy       = self.jsonEntities.defaultStudy();

      self.study               = new self.Study(self.jsonStudy);
      self.participant         = new self.Participant(self.jsonParticipant);
      self.collectionEventType = new self.CollectionEventType(self.jsonCeventType);
      self.collectionEvent    = new self.CollectionEvent(self.jsonCevent);
    }));

    it('has valid scope', function() {
      var directive = createDirective(this);

      expect(directive.controller.study).toBe(this.study);
      expect(directive.controller.participant).toBe(this.participant);
      expect(directive.controller.collectionEventType).toBe(this.collectionEventType);

      expect(directive.controller.collectionEvent).toBeDefined();
      expect(directive.controller.title).toBeDefined();
      expect(directive.controller.timeCompleted).toBeObject();
      expect(directive.controller.timeCompleted.date).toBeDate();
      expect(directive.controller.timeCompleted.time).toBeDate();

      expect(directive.controller.submit).toBeFunction();
      expect(directive.controller.cancel).toBeFunction();
    });

    it('on submit success changes state', function() {
      var directive;

      spyOn(this.CollectionEvent.prototype, 'add').and.returnValue(this.$q.when(this.collectionEvent));
      spyOn(this.$state, 'go').and.returnValue('ok');

      directive = createDirective(this);
      directive.controller.submit();
      directive.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith(
        'home.collection.study.participant.cevents', {}, { reload: true });
    });

    describe('on submit failure', function() {

      it('displays an error modal', function() {
        var directive,
            updateDeferred = this.$q.defer();

        updateDeferred.reject('simulated update failure');
        spyOn(this.CollectionEvent.prototype, 'add').and.returnValue(updateDeferred.promise);
        spyOn(this.domainEntityService, 'updateErrorModal').and.returnValue(this.$q.when('ok'));

        directive = createDirective(this);
        directive.controller.submit();
        directive.scope.$digest();

        expect(this.domainEntityService.updateErrorModal).toHaveBeenCalled();
      });

      it('changes state when Cancel button pressed on error modal', function() {
        var directive,
            updateDeferred = this.$q.defer(),
            errorModalDeferred = this.$q.defer();

        updateDeferred.reject('simulated update failure');
        errorModalDeferred.reject('cancel button pressed');

        spyOn(this.CollectionEvent.prototype, 'add').and.returnValue(updateDeferred.promise);
        spyOn(this.domainEntityService, 'updateErrorModal').and.returnValue(errorModalDeferred.promise);
        spyOn(this.$state, 'go').and.returnValue('ok');

        directive = createDirective(this);
        directive.controller.submit();
        directive.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith(
        'home.collection.study.participant', { participantId: this.participant.id });
      });

    });

    it('changes state when forms Cancel button is pressed', function() {
      var directive;

      spyOn(this.$state, 'go').and.returnValue('ok');

      directive = createDirective(this);
      directive.controller.cancel();
      directive.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith('home.collection.study.participant.cevents');
    });

  });

});
