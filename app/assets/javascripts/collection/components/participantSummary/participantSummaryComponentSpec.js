/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import annotationUpdateSharedBehaviour from 'test/behaviours/annotationUpdateSharedBehaviour';
import faker  from 'faker';
import ngModule from '../../index'

describe('Component: participantSummary', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'Study',
                              'Participant',
                              'AnnotationValueType',
                              'AnnotationMaxValueCount',
                              'Factory');

      this.jsonParticipant = this.Factory.participant();
      this.jsonStudy       = this.Factory.defaultStudy();

      this.participant = new this.Participant(this.jsonParticipant);
      this.study       = new this.Study(this.jsonStudy);

      this.participantWithAnnotation = (valueType, maxValueCount) => {
        var jsonAnnotationType,
            value,
            jsonAnnotation,
            jsonStudy,
            jsonParticipant,
            study;

        maxValueCount = maxValueCount || 0;

        jsonAnnotationType = this.Factory.annotationType({ valueType: valueType,
                                                           maxValueCount: maxValueCount });
        value              = this.Factory.valueForAnnotation(jsonAnnotationType);
        jsonAnnotation     = this.Factory.annotation({ value: value }, jsonAnnotationType);
        jsonStudy          = this.Factory.study({ annotationTypes: [ jsonAnnotationType ]});
        jsonParticipant    = this.Factory.participant({ annotations: [ jsonAnnotation ] });
        study              = this.Study.create(jsonStudy);

        return new this.Participant(jsonParticipant, study);
      };

      this.createController = (study, participant) => {
        study = study || this.study;
        participant = participant || this.participant;

        ComponentTestSuiteMixin.createController.call(
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
    });
  });

  it('has valid scope', function() {
    this.createController(this.study, this.participant);

    expect(this.controller.study).toBe(this.study);
    expect(this.controller.participant).toBe(this.participant);

    expect(this.controller.editUniqueId).toBeFunction();
    expect(this.controller.editAnnotation).toBeFunction();
  });

  describe('updates to time completed', function () {

    var context = {};

    beforeEach(function () {
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
    });

    sharedUpdateBehaviour(context);

  });

  describe('updates to annotations', function () {

    var context = {};

    beforeEach(function () {
      context.entity                   = this.Participant;
      context.entityUpdateFuncName     = 'addAnnotation';
    });

    describe('updates to a text annotation', function () {

      beforeEach(function () {
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
      });

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a date time annotation', function () {

      beforeEach(function () {
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
      });

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a number annotation', function () {

      beforeEach(function () {
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
      });

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a single select annotation', function () {

      beforeEach(function () {
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
      });

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a multiple select annotation', function () {

      beforeEach(function () {
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
      });

      annotationUpdateSharedBehaviour(context);

    });

  });

  function sharedUpdateBehaviour(context) {

    describe('(shared) tests', function() {

      beforeEach(function() {
        this.injectDependencies('Participant', 'modalInput', 'notificationsService');
      });


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
        this.createController();

        spyOn(this.modalInput, context.modalInputFuncName)
          .and.returnValue({ result: this.$q.resolve(context.newValue)});
        spyOn(this.Participant.prototype, context.participantUpdateFuncName)
          .and.returnValue(this.$q.reject('simulated error'));
        spyOn(this.notificationsService, 'updateError').and.returnValue(this.$q.when('OK'));

        this.controller[context.controllerUpdateFuncName]();
        this.scope.$digest();

        expect(this.notificationsService.updateError).toHaveBeenCalled();
      });

    });

  }

});
