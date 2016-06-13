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

    var createDirective = function () {
      this.element = angular.element([
        '<cevent-add',
        '  study="vm.study"',
        '  participant="vm.participant"',
        '  collection-event-type="vm.collectionEventType">',
        '</cevent-add>'
      ].join(''));

      this.scope = this.$rootScope.$new();
      this.scope.vm = {
        study:               this.study,
        participant:         this.participant,
        collectionEventType: this.collectionEventType
      };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('ceventAdd');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'domainEntityService',
                              'Study',
                              'Participant',
                              'CollectionEvent',
                              'CollectionEventType',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/ceventAdd/ceventAdd.html',
        '/assets/javascripts/common/annotationsInput/annotationsInput.html',
        '/assets/javascripts/common/annotationsInput/textAnnotation.html',
        '/assets/javascripts/common/annotationsInput/numberAnnotation.html',
        '/assets/javascripts/common/annotationsInput/dateTimeAnnotation.html',
        '/assets/javascripts/common/annotationsInput/singleSelectAnnotation.html',
        '/assets/javascripts/common/annotationsInput/multipleSelectAnnotation.html');

      self.jsonCevent      = self.factory.collectionEvent();
      self.jsonCeventType  = self.factory.defaultCollectionEventType();
      self.jsonParticipant = self.factory.defaultParticipant();
      self.jsonStudy       = self.factory.defaultStudy();

      self.study               = new self.Study(self.jsonStudy);
      self.participant         = new self.Participant(self.jsonParticipant);
      self.collectionEventType = new self.CollectionEventType(self.jsonCeventType);
      self.collectionEvent    = new self.CollectionEvent(self.jsonCevent);
    }));

    it('has valid scope', function() {
      createDirective.call(this);

      expect(this.controller.study).toBe(this.study);
      expect(this.controller.participant).toBe(this.participant);
      expect(this.controller.collectionEventType).toBe(this.collectionEventType);

      expect(this.controller.collectionEvent).toBeDefined();
      expect(this.controller.title).toBeDefined();
      expect(this.controller.timeCompleted).toBeDate();

      expect(this.controller.submit).toBeFunction();
      expect(this.controller.cancel).toBeFunction();
    });

    it('on submit success changes state', function() {
      spyOn(this.CollectionEvent.prototype, 'add').and.returnValue(this.$q.when(this.collectionEvent));
      spyOn(this.$state, 'go').and.returnValue('ok');

      createDirective.call(this);
      this.controller.submit();
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith(
        'home.collection.study.participant.cevents', {}, { reload: true });
    });

    describe('on submit failure', function() {

      it('displays an error modal', function() {
        spyOn(this.CollectionEvent.prototype, 'add')
          .and.returnValue(this.$q.reject('simulated update failure'));
        spyOn(this.domainEntityService, 'updateErrorModal').and.returnValue(this.$q.when('ok'));

        createDirective.call(this);
        this.controller.submit();
        this.scope.$digest();

        expect(this.domainEntityService.updateErrorModal).toHaveBeenCalled();
      });

      it('changes state when Cancel button pressed on error modal', function() {
        spyOn(this.CollectionEvent.prototype, 'add')
          .and.returnValue(this.$q.reject('simulated update failure'));
        spyOn(this.domainEntityService, 'updateErrorModal')
          .and.returnValue(this.$q.reject('cancel button pressed'));
        spyOn(this.$state, 'go').and.returnValue('ok');

        createDirective.call(this);
        this.controller.submit();
        this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith(
        'home.collection.study.participant', { participantId: this.participant.id });
      });

    });

    it('changes state when forms Cancel button is pressed', function() {
      spyOn(this.$state, 'go').and.returnValue('ok');

      createDirective.call(this);
      this.controller.cancel();
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith('home.collection.study.participant.cevents');
    });

  });

});
