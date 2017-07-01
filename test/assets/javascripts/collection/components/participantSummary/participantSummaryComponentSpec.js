/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function(require) {
  'use strict';

  var mocks                           = require('angularMocks'),
      _                               = require('lodash'),
      faker                           = require('faker'),
      annotationUpdateSharedBehaviour = require('../../../test/annotationUpdateSharedBehaviour');

  describe('Component: participantSummary', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
        ComponentTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.participantWithAnnotation = function (valueType, maxValueCount) {
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
        jsonParticipant    = this.factory.participant({ annotations: [ jsonAnnotation ] });
        study              = this.Study.create(jsonStudy);

        return new this.Participant(jsonParticipant, study);
      };

      SuiteMixin.prototype.createController = function (study, participant) {
        study = study || this.study;
        participant = participant || this.participant;

        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          [
            '<participant-summary',
            '  study="vm.study"',
            '  participant="vm.participant">',
            '</participant-summary>'
          ].join(''),
          {
            study:       study,
            participant: participant
          },
          'participantSummary');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'Study',
                              'Participant',
                              'AnnotationValueType',
                              'AnnotationMaxValueCount',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/collection/components/participantSummary/participantSummary.html',
        '/assets/javascripts/common/components/statusLine/statusLine.html');

      this.jsonParticipant = this.factory.participant();
      this.jsonStudy       = this.factory.defaultStudy();

      this.participant = new this.Participant(this.jsonParticipant);
      this.study       = new this.Study(this.jsonStudy);
    }));

    it('has valid scope', function() {
      this.createController(this.study, this.participant);

      expect(this.controller.study).toBe(this.study);
      expect(this.controller.participant).toBe(this.participant);

      expect(this.controller.editUniqueId).toBeFunction();
      expect(this.controller.editAnnotation).toBeFunction();
    });

    describe('updates to time completed', function () {

      var context = {};

      beforeEach(inject(function () {
        var self = this;

        context.createController           = createController;
        context.controllerUpdateFuncName  = 'editUniqueId';
        context.modalInputFuncName        = 'text';
        context.participantUpdateFuncName = 'updateUniqueId';
        context.participant               = self.participant;
        context.newValue                  = faker.random.word();

        function createController() {
          return self.createController(self.study, self.participant);
        }
      }));

      sharedUpdateBehaviour(context);

    });

    describe('updates to annotations', function () {

      var context = {};

      beforeEach(inject(function () {
        context.entity                   = this.Participant;
        context.entityUpdateFuncName     = 'addAnnotation';
      }));

      describe('updates to a text annotation', function () {

        beforeEach(inject(function () {
          var self = this,
              participant = self.participantWithAnnotation(self.AnnotationValueType.TEXT);

          context.createController           = createController;
          context.controllerUpdateFuncName = 'editAnnotation';
          context.modalInputFuncName       = 'text';
          context.annotation               = participant.annotations[0];
          context.newValue                 = faker.random.word();

          function createController() {
            return self.createController(self.study, participant);
          }
        }));

        annotationUpdateSharedBehaviour(context);

      });

      describe('updates to a date time annotation', function () {

        beforeEach(inject(function () {
          var self = this,
              participant = self.participantWithAnnotation(self.AnnotationValueType.DATE_TIME);

          context.createController           = createController;
          context.controllerUpdateFuncName = 'editAnnotation';
          context.modalInputFuncName       = 'dateTime';
          context.annotation               = participant.annotations[0];
          context.newValue                 = faker.date.recent(10);

          function createController() {
            return self.createController(self.study, participant);
          }
        }));

        annotationUpdateSharedBehaviour(context);

      });

      describe('updates to a number annotation', function () {

        beforeEach(inject(function () {
          var self = this,
              participant = self.participantWithAnnotation(self.AnnotationValueType.NUMBER);

          context.createController           = createController;
          context.controllerUpdateFuncName = 'editAnnotation';
          context.modalInputFuncName       = 'number';
          context.annotation               = participant.annotations[0];
          context.newValue                 = 10;

          function createController() {
            return self.createController(self.study, participant);
          }
        }));

        annotationUpdateSharedBehaviour(context);

      });

      describe('updates to a single select annotation', function () {

        beforeEach(inject(function () {
          var self = this,
              participant = self.participantWithAnnotation(self.AnnotationValueType.SELECT,
                                                           self.AnnotationMaxValueCount.SELECT_SINGLE);

          context.createController           = createController;
          context.controllerUpdateFuncName = 'editAnnotation';
          context.modalInputFuncName       = 'select';
          context.annotation               = participant.annotations[0];
          context.newValue                 = participant.annotations[0].annotationType.options[0];

          function createController() {
            return self.createController(self.study, participant);
          }
        }));

        annotationUpdateSharedBehaviour(context);

      });

      describe('updates to a multiple select annotation', function () {

        beforeEach(inject(function () {
          var self = this,
              participant = self.participantWithAnnotation(self.AnnotationValueType.SELECT,
                                                           self.AnnotationMaxValueCount.SELECT_MULTIPLE);

          context.createController           = createController;
          context.controllerUpdateFuncName = 'editAnnotation';
          context.modalInputFuncName       = 'selectMultiple';
          context.annotation               = participant.annotations[0];
          context.newValue                 = participant.annotations[0].annotationType.options;

          function createController() {
            return self.createController(self.study, participant);
          }
        }));

        annotationUpdateSharedBehaviour(context);

      });

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

          this.createController();
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

          this.createController();
          this.controller[context.controllerUpdateFuncName]();
          this.scope.$digest();

          expect(this.notificationsService.updateError).toHaveBeenCalled();
        });

      });

    }

  });

});
