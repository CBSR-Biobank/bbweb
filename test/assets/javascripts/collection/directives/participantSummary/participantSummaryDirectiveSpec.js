/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function(require) {
  'use strict';

  var angular                         = require('angular'),
      mocks                           = require('angularMocks'),
      _                               = require('lodash'),
      faker                           = require('faker'),
      annotationUpdateSharedBehaviour = require('../../../common/annotationUpdateSharedBehaviourSpec');

  describe('participantSummaryDirective', function() {

    var participantWithAnnotation = function (valueType, maxValueCount) {
      var jsonAnnotationType,
          value,
          jsonAnnotation,
          jsonStudy,
          jsonParticipant,
          study;

      maxValueCount = maxValueCount || 0;

      jsonAnnotationType = this.factory.annotationType({ valueType: valueType,
                                                         maxValueCount: maxValueCount });
      value              = this.factory.valueForAnnotation(jsonAnnotationType);
      jsonAnnotation     = this.factory.annotation({ value: value }, jsonAnnotationType);
      jsonStudy          = this.factory.study({ annotationTypes: [ jsonAnnotationType ]});
      jsonParticipant    = this.factory.participant({ annotations: [ jsonAnnotation ]});

      study = new this.Study(jsonStudy);

      return new this.Participant(jsonParticipant, study);
    };

    var createDirective = function (study, participant) {
      study = study || this.study;
      participant = participant || this.participant;

      this.element = angular.element([
        '<participant-summary',
        '  study="vm.study"',
        '  participant="vm.participant">',
        '</participant-summary>'
      ].join(''));

      this.scope = this.$rootScope.$new();
      this.scope.vm = {
        study:       study,
        participant: participant
      };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('participantSummary');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'Study',
                              'Participant',
                              'AnnotationValueType',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/participantSummary/participantSummary.html',
        '/assets/javascripts/admin/directives/statusLine/statusLine.html');

      self.jsonParticipant = self.factory.participant();
      self.jsonStudy       = self.factory.defaultStudy();

      self.participant = new self.Participant(self.jsonParticipant);
      self.study       = new self.Study(self.jsonStudy);
    }));

    it('has valid scope', function() {
      createDirective.call(this, this.study, this.participant);

      expect(this.controller.study).toBe(this.study);
      expect(this.controller.participant).toBe(this.participant);

      expect(this.controller.editUniqueId).toBeFunction();
      expect(this.controller.editAnnotation).toBeFunction();
    });

    describe('updates to time completed', function () {

      var context = {};

      beforeEach(inject(function () {
        context.createDirective           = createDirective;
        context.controllerUpdateFuncName  = 'editUniqueId';
        context.modalInputFuncName        = 'text';
        context.participantUpdateFuncName = 'updateUniqueId';
        context.participant               = this.participant;
        context.newValue                  = faker.random.word();
      }));

      sharedUpdateBehaviour(context);

    });

    describe('updates to a text annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var participant = participantWithAnnotation.call(this, this.AnnotationValueType.TEXT);

        context.createDirective           = createDirective;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'text';
        context.entity                   = this.Participant;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.annotation               = participant.annotations[0];
        context.newValue                 = faker.random.word();
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a date time annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var participant = participantWithAnnotation.call(this, this.AnnotationValueType.DATE_TIME);

        context.createDirective           = createDirective;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'dateTime';
        context.entity                   = this.Participant;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.annotation               = participant.annotations[0];
        context.newValue                 = faker.date.recent(10);
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a number annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var participant = participantWithAnnotation.call(this, this.AnnotationValueType.NUMBER);

        context.createDirective           = createDirective;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'number';
        context.entity                   = this.Participant;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.annotation               = participant.annotations[0];
        context.newValue                 = 10;
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a single select annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var participant = participantWithAnnotation.call(this, this.AnnotationValueType.SELECT, 1);

        context.createDirective           = createDirective;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'select';
        context.entity                   = this.Participant;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.annotation               = participant.annotations[0];
        context.newValue                 = participant.annotations[0].annotationType.options[0];
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a multiple select annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var participant = participantWithAnnotation.call(this, this.AnnotationValueType.SELECT, 2);

        context.createDirective           = createDirective;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'selectMultiple';
        context.entity                   = this.Participant;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.annotation               = participant.annotations[0];
        context.newValue                 = participant.annotations[0].annotationType.options;
      }));

      annotationUpdateSharedBehaviour(context);

    });

    function sharedUpdateBehaviour(context) {

      describe('(shared) tests', function() {

        beforeEach(inject(function() {
          this.injectDependencies('Participant', 'modalInput', 'notificationsService');
        }));


        it('on update should invoke the update method on entity', function() {
          var deferred = this.$q.defer();

          deferred.resolve(context.newValue);

          spyOn(this.modalInput, context.modalInputFuncName)
            .and.returnValue({ result: deferred.promise});
          spyOn(this.Participant.prototype, context.participantUpdateFuncName)
            .and.returnValue(this.$q.when(context.participant));
          spyOn(this.notificationsService, 'success').and.returnValue(this.$q.when('OK'));

          createDirective.call(this);
          this.controller[context.controllerUpdateFuncName]();
          this.scope.$digest();

          expect(this.Participant.prototype[context.participantUpdateFuncName]).toHaveBeenCalled();
          expect(this.notificationsService.success).toHaveBeenCalled();
        });

        it('error message should be displayed when update fails', function() {
          var modalDeferred = this.$q.defer(),
              updateDeferred = this.$q.defer();

          modalDeferred.resolve(context.newValue);
          updateDeferred.reject('simulated error');

          spyOn(this.modalInput, context.modalInputFuncName)
            .and.returnValue({ result: modalDeferred.promise});
          spyOn(this.Participant.prototype, context.participantUpdateFuncName)
            .and.returnValue(updateDeferred.promise);
          spyOn(this.notificationsService, 'updateError').and.returnValue(this.$q.when('OK'));

          createDirective.call(this);
          this.controller[context.controllerUpdateFuncName]();
          this.scope.$digest();

          expect(this.notificationsService.updateError).toHaveBeenCalled();
        });

      });

    }

  });

});
