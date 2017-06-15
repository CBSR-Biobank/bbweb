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

  describe('participantAddDirective', function() {

    var createDirective = function () {
      this.element = angular.element([
        '<participant-add',
        ' study="vm.study"',
        ' unique-id="' + this.uniqueId + '">',
        '</participant-add>'
      ].join(''));

      this.scope = this.$rootScope.$new();
      this.scope.vm = { study: this.study };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('participantAdd');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var self = this;

      _.extend(self, TestSuiteMixin.prototype);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'domainNotificationService',
                              'notificationsService',
                              'Study',
                              'Participant',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/participantAdd/participantAdd.html',
        '/assets/javascripts/common/annotationsInput/annotationsInput.html',
        '/assets/javascripts/common/annotationsInput/dateTimeAnnotation.html',
        '/assets/javascripts/common/annotationsInput/multipleSelectAnnotation.html',
        '/assets/javascripts/common/annotationsInput/numberAnnotation.html',
        '/assets/javascripts/common/annotationsInput/singleSelectAnnotation.html',
        '/assets/javascripts/common/annotationsInput/textAnnotation.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');

      self.jsonParticipant = self.factory.participant();
      self.jsonStudy       = self.factory.defaultStudy();
      self.study           = new self.Study(self.jsonStudy);
      self.uniqueId        = self.jsonParticipant.uniqueId;

      spyOn(this.$state, 'go').and.returnValue('ok');
    }));

    it('has valid scope', function() {
      createDirective.call(this);

      expect(this.controller.study).toBe(this.study);
      expect(this.controller.uniqueId).toBe(this.uniqueId);

      expect(this.controller.participant).toEqual(jasmine.any(this.Participant));

      expect(this.controller.submit).toBeFunction();
      expect(this.controller.cancel).toBeFunction();
    });

    describe('on submit', function() {

      beforeEach(function() {
        this.participant = new this.Participant(this.jsonParticipant);
      });

      it('changes to correct state on valid submit', function() {
        spyOn(this.Participant.prototype, 'add').and.returnValue(this.$q.when(this.participant));

        createDirective.call(this);
        this.controller.submit(this.participant);
        this.scope.$digest();

        expect(this.$state.go).toHaveBeenCalledWith(
          'home.collection.study.participant.summary',
          { studyId: this.study.id, participantId: this.participant.id },
          { reload: true });
      });

      describe('when submit fails', function() {

        it('displays an error modal', function() {
          var participantAddDeferred = this.$q.defer();

          participantAddDeferred.reject('submit failure');

          spyOn(this.Participant.prototype, 'add').and.returnValue(participantAddDeferred.promise);
          spyOn(this.domainNotificationService, 'updateErrorModal').and.returnValue(this.$q.when('OK'));

          createDirective.call(this);
          this.controller.submit(this.participant);
          this.scope.$digest();

          expect(this.domainNotificationService.updateErrorModal).toHaveBeenCalled();
        });

        it('user presses Cancel on error modal', function() {
          var participantAddDeferred = this.$q.defer(),
              updateErrorModalDeferred = this.$q.defer();

          participantAddDeferred.reject('submit failure');
          updateErrorModalDeferred.reject('Cancel');

          spyOn(this.Participant.prototype, 'add').and.returnValue(participantAddDeferred.promise);
          spyOn(this.domainNotificationService, 'updateErrorModal')
            .and.returnValue(updateErrorModalDeferred.promise);

          createDirective.call(this);
          this.controller.submit(this.participant);
          this.scope.$digest();

          expect(this.domainNotificationService.updateErrorModal).toHaveBeenCalled();
          expect(this.$state.go).toHaveBeenCalledWith(
            'home.collection.study',
            { studyId: this.study.id });
        });

      });

    });

    it('changes to correct state on cancel', function() {
        createDirective.call(this);
        this.controller.cancel();
        this.scope.$digest();

        expect(this.$state.go).toHaveBeenCalledWith(
          'home.collection.study',
          { studyId: this.study.id });

    });

  });

});
