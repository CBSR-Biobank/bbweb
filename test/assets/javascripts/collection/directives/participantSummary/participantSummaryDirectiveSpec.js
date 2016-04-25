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
      _                               = require('underscore'),
      faker                           = require('faker'),
      annotationUpdateSharedBehaviour = require('../../../common/annotationUpdateSharedBehaviourSpec');

  function createDirective(test) {
    var element,
        scope = test.$rootScope.$new();

    element = angular.element([
      '<participant-summary',
      '  study="vm.study"',
      '  participant="vm.participant">',
      '</participant-summary>'
    ].join(''));

    scope.vm = {
      study:       test.study,
      participant: test.participant
    };
    test.$compile(element)(scope);
    scope.$digest();

    return {
      element:    element,
      scope:      scope,
      controller: element.controller('participantSummary')
    };
  }

  describe('participantSummaryDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(directiveTestSuite) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$q                  = self.$injector.get('$q');
      self.$rootScope          = self.$injector.get('$rootScope');
      self.$compile            = self.$injector.get('$compile');
      self.$state              = self.$injector.get('$state');
      self.Study               = self.$injector.get('Study');
      self.Participant         = self.$injector.get('Participant');
      self.AnnotationValueType = self.$injector.get('AnnotationValueType');
      self.jsonEntities        = self.$injector.get('jsonEntities');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/participantSummary/participantSummary.html',
        '/assets/javascripts/admin/directives/statusLine/statusLine.html');

      self.jsonParticipant = self.jsonEntities.participant();
      self.jsonStudy       = self.jsonEntities.defaultStudy();

      self.participant = new self.Participant(self.jsonParticipant);
      self.study       = new self.Study(self.jsonStudy);

      self.participantWithAnnotation = participantWithAnnotation;

      //---

      function participantWithAnnotation(valueType, maxValueCount) {
        var jsonAnnotationType,
            value,
            jsonAnnotation,
            jsonStudy,
            jsonParticipant,
            study;

        maxValueCount = maxValueCount || 0;

        jsonAnnotationType = self.jsonEntities.annotationType({ valueType: valueType,
                                                                maxValueCount: maxValueCount });
        value              = self.jsonEntities.valueForAnnotation(jsonAnnotationType);
        jsonAnnotation     = self.jsonEntities.annotation({ value: value }, jsonAnnotationType);
        jsonStudy          = self.jsonEntities.study({ annotationTypes: [ jsonAnnotationType ]});
        jsonParticipant    = self.jsonEntities.participant({ annotations: [ jsonAnnotation ]});

        study = new self.Study(jsonStudy);

        return new self.Participant(jsonParticipant, study);
      }
    }));

    it('has valid scope', function() {
      var directive = createDirective(this);

      expect(directive.controller.study).toBe(this.study);
      expect(directive.controller.participant).toBe(this.participant);

      expect(directive.controller.editUniqueId).toBeFunction();
      expect(directive.controller.editAnnotation).toBeFunction();
    });

    describe('updates to time completed', function () {

      var context = {};

      beforeEach(inject(function () {
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
        var participant = this.participantWithAnnotation(this.AnnotationValueType.TEXT);

        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'text';
        context.entity                   = this.Participant;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.createDirective          = createDirective;
        context.annotation               = participant.annotations[0];
        context.newValue                 = faker.random.word();
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a date time annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var participant = this.participantWithAnnotation(this.AnnotationValueType.DATE_TIME);

        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'dateTime';
        context.entity                   = this.Participant;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.createDirective          = createDirective;
        context.annotation               = participant.annotations[0];
        context.newValue                 = faker.date.recent(10);
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a number annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var participant = this.participantWithAnnotation(this.AnnotationValueType.NUMBER);

        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'number';
        context.entity                   = this.Participant;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.createDirective          = createDirective;
        context.annotation               = participant.annotations[0];
        context.newValue                 = 10;
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a single select annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var participant = this.participantWithAnnotation(this.AnnotationValueType.SELECT, 1);

        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'select';
        context.entity                   = this.Participant;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.createDirective          = createDirective;
        context.annotation               = participant.annotations[0];
        context.newValue                 = participant.annotations[0].annotationType.options[0];
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a multiple select annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var participant = this.participantWithAnnotation(this.AnnotationValueType.SELECT, 2);

        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'selectMultiple';
        context.entity                   = this.Participant;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.createDirective          = createDirective;
        context.annotation               = participant.annotations[0];
        context.newValue                 = participant.annotations[0].annotationType.options;
      }));

      annotationUpdateSharedBehaviour(context);

    });

  });

  function sharedUpdateBehaviour(context) {

    describe('(shared) tests', function() {

      beforeEach(inject(function() {
        this.Participant          = this.$injector.get('Participant');
        this.modalInput           = this.$injector.get('modalInput');
        this.notificationsService = this.$injector.get('notificationsService');
      }));


      it('on update should invoke the update method on entity', function() {
        var directive,
            deferred = this.$q.defer();

        deferred.resolve(context.newValue);

        spyOn(this.modalInput, context.modalInputFuncName)
          .and.returnValue({ result: deferred.promise});
        spyOn(this.Participant.prototype, context.participantUpdateFuncName)
          .and.returnValue(this.$q.when(context.participant));
        spyOn(this.notificationsService, 'success').and.returnValue(this.$q.when('OK'));

        directive = createDirective(this);
        directive.controller[context.controllerUpdateFuncName]();
        directive.scope.$digest();

        expect(this.Participant.prototype[context.participantUpdateFuncName]).toHaveBeenCalled();
        expect(this.notificationsService.success).toHaveBeenCalled();
      });

      it('error message should be displayed when update fails', function() {
        var directive,
            modalDeferred = this.$q.defer(),
            updateDeferred = this.$q.defer();

        modalDeferred.resolve(context.newValue);
        updateDeferred.reject('simulated error');

        spyOn(this.modalInput, context.modalInputFuncName)
          .and.returnValue({ result: modalDeferred.promise});
        spyOn(this.Participant.prototype, context.participantUpdateFuncName)
          .and.returnValue(updateDeferred.promise);
        spyOn(this.notificationsService, 'updateError').and.returnValue(this.$q.when('OK'));

        directive = createDirective(this);
        directive.controller[context.controllerUpdateFuncName]();
        directive.scope.$digest();

        expect(this.notificationsService.updateError).toHaveBeenCalled();
      });

    });

  }

});
