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

  describe('participantAddDirective', function() {

    function createDirective(test) {
      var element,
          scope = test.$rootScope.$new();

      element = angular.element([
        '<participant-add',
        ' study="vm.study"',
        ' unique-id="' + test.uniqueId + '">',
        '</participant-add>'
      ].join(''));

      scope.vm = { study: test.study };
      test.$compile(element)(scope);
      scope.$digest();

      return {
        element:    element,
        scope:      scope,
        controller: element.controller('participantAdd')
      };
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(templateMixin) {
      var self = this;

      _.extend(self, templateMixin);

      self.$q                   = self.$injector.get('$q');
      self.$rootScope           = self.$injector.get('$rootScope');
      self.$compile             = self.$injector.get('$compile');
      self.$state               = self.$injector.get('$state');
      self.domainEntityService  = self.$injector.get('domainEntityService');
      self.notifiactionsService = self.$injector.get('notificationsService');
      self.Study                = self.$injector.get('Study');
      self.Participant          = self.$injector.get('Participant');
      self.jsonEntities         = self.$injector.get('jsonEntities');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/participantAdd/participantAdd.html',
        '/assets/javascripts/common/annotationsInput/annotationsInput.html',
        '/assets/javascripts/common/annotationsInput/dateTimeAnnotation.html',
        '/assets/javascripts/common/annotationsInput/multipleSelectAnnotation.html',
        '/assets/javascripts/common/annotationsInput/numberAnnotation.html',
        '/assets/javascripts/common/annotationsInput/singleSelectAnnotation.html',
        '/assets/javascripts/common/annotationsInput/textAnnotation.html');

      self.jsonParticipant = self.jsonEntities.participant();
      self.jsonStudy       = self.jsonEntities.defaultStudy();
      self.study           = new self.Study(self.jsonStudy);
      self.uniqueId        = self.jsonParticipant.uniqueId;

      spyOn(this.$state, 'go').and.returnValue('ok');
    }));

    it('has valid scope', function() {
      var directive = createDirective(this);

      expect(directive.controller.study).toBe(this.study);
      expect(directive.controller.uniqueId).toBe(this.uniqueId);

      expect(directive.controller.participant).toEqual(jasmine.any(this.Participant));

      expect(directive.controller.submit).toBeFunction();
      expect(directive.controller.cancel).toBeFunction();
    });

    describe('on submit', function() {

      beforeEach(function() {
        this.participant = new this.Participant(this.jsonParticipant);
      });

      it('changes to correct state on valid submit', function() {
        var directive;

        spyOn(this.Participant.prototype, 'add').and.returnValue(this.$q.when(this.participant));

        directive = createDirective(this);
        directive.controller.submit(this.participant);
        directive.scope.$digest();

        expect(this.$state.go).toHaveBeenCalledWith(
          'home.collection.study.participant.summary',
          { studyId: this.study.id, participantId: this.participant.id },
          { reload: true });
      });

      describe('when submit fails', function() {

        it('displays an error modal', function() {
          var directive,
              participantAddDeferred = this.$q.defer();

          participantAddDeferred.reject('submit failure');

          spyOn(this.Participant.prototype, 'add').and.returnValue(participantAddDeferred.promise);
          spyOn(this.domainEntityService, 'updateErrorModal').and.returnValue(this.$q.when('OK'));

          directive = createDirective(this);
          directive.controller.submit(this.participant);
          directive.scope.$digest();

          expect(this.domainEntityService.updateErrorModal).toHaveBeenCalled();
        });

        it('user presses Cancel on error modal', function() {
          var directive,
              participantAddDeferred = this.$q.defer(),
              updateErrorModalDeferred = this.$q.defer();

          participantAddDeferred.reject('submit failure');
          updateErrorModalDeferred.reject('Cancel');

          spyOn(this.Participant.prototype, 'add').and.returnValue(participantAddDeferred.promise);
          spyOn(this.domainEntityService, 'updateErrorModal')
            .and.returnValue(updateErrorModalDeferred.promise);

          directive = createDirective(this);
          directive.controller.submit(this.participant);
          directive.scope.$digest();

          expect(this.domainEntityService.updateErrorModal).toHaveBeenCalled();
          expect(this.$state.go).toHaveBeenCalledWith(
            'home.collection.study',
            { studyId: this.study.id });
        });

      });

    });

    it('changes to correct state on cancel', function() {
        var directive;

        directive = createDirective(this);
        directive.controller.cancel();
        directive.scope.$digest();

        expect(this.$state.go).toHaveBeenCalledWith(
          'home.collection.study',
          { studyId: this.study.id });

    });

  });

});
