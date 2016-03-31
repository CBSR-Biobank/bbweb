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
      '<cevent-view',
      '  collection-event-types="vm.collectionEventTypes"',
      '  collection-event="vm.collectionEvent">',
      '</cevent-view>'
    ].join(''));

    scope.vm = {
      collectionEventTypes: test.collectionEventTypes,
      collectionEvent:      test.collectionEvent
    };
    test.$compile(element)(scope);
    scope.$digest();

    return {
      element:    element,
      scope:      scope,
      controller: element.controller('ceventView')
    };
  }

  describe('ceventViewDirectiveSpec', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(directiveTestSuite) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$q                  = self.$injector.get('$q');
      self.$rootScope          = self.$injector.get('$rootScope');
      self.$compile            = self.$injector.get('$compile');
      self.$state              = self.$injector.get('$state');
      self.Participant         = self.$injector.get('Participant');
      self.CollectionEvent     = self.$injector.get('CollectionEvent');
      self.CollectionEventType = self.$injector.get('CollectionEventType');
      self.AnnotationValueType = self.$injector.get('AnnotationValueType');
      self.jsonEntities        = self.$injector.get('jsonEntities');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/ceventView/ceventView.html',
        '/assets/javascripts/admin/directives/statusLine/statusLine.html');

      self.jsonCevent      = self.jsonEntities.collectionEvent();
      self.jsonParticipant = self.jsonEntities.defaultParticipant();
      self.jsonCeventType  = self.jsonEntities.defaultCollectionEventType();

      self.participant          = new self.Participant(self.jsonParticipant);
      self.collectionEvent      = new self.CollectionEvent(self.jsonCevent);
      self.pagedResult          = self.jsonEntities.pagedResult([ self.collectionEvent ]);
      self.collectionEventTypes = [ new self.CollectionEventType(self.jsonCeventType) ];

      self.collectionEventWithAnnotation = collectionEventWithAnnotation;

      //---

      function collectionEventWithAnnotation(valueType, maxValueCount) {
        var jsonAnnotationType,
            value,
            jsonAnnotation,
            jsonCeventType,
            jsonCevent,
            collectionEventType;

        maxValueCount = maxValueCount || 0;

        jsonAnnotationType = self.jsonEntities.annotationType({ valueType: valueType,
                                                                maxValueCount: maxValueCount });
        value              = self.jsonEntities.valueForAnnotation(jsonAnnotationType);
        jsonAnnotation     = self.jsonEntities.annotation({ value: value }, jsonAnnotationType);
        jsonCeventType     = self.jsonEntities.collectionEventType({ annotationTypes: [ jsonAnnotationType ]});
        jsonCevent         = self.jsonEntities.collectionEvent({ annotations: [ jsonAnnotation ]});
        collectionEventType = new self.CollectionEventType(jsonCeventType);
        return new self.CollectionEvent(jsonCevent, collectionEventType);
      }
    }));

    it('has valid scope', function() {
      var directive = createDirective(this);

      expect(directive.controller.collectionEventTypes).toBe(this.collectionEventTypes);
      expect(directive.controller.collectionEvent).toBe(this.collectionEvent);
      expect(directive.controller.panelOpen).toBeTrue();
      expect(directive.controller.timeCompletedLocal).toBeDefined();

      expect(directive.controller.editVisitType).toBeFunction();
      expect(directive.controller.editTimeCompleted).toBeFunction();
      expect(directive.controller.editAnnotation).toBeFunction();
      expect(directive.controller.addSpecimen).toBeFunction();
      expect(directive.controller.panelButtonClicked).toBeFunction();
    });

    it('panel can be closed and opened', function() {
      var directive = createDirective(this);

      directive.controller.panelButtonClicked();
      directive.scope.$digest();
      expect(directive.controller.panelOpen).toBeFalse();

      directive.controller.panelButtonClicked();
      directive.scope.$digest();
      expect(directive.controller.panelOpen).toBeTrue();
    });

    describe('updates to time completed', function () {

      var context = {};

      beforeEach(inject(function () {
        context.controllerUpdateFuncName = 'editTimeCompleted';
        context.modalInputFuncName       = 'dateTime';
        context.ceventUpdateFuncName     = 'updateTimeCompleted';
        context.collectionEvent          = this.collectionEvent;
        context.newValue                 = faker.date.recent(10);
      }));

      sharedUpdateBehaviour(context);

    });

    describe('updates to a text annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.TEXT());

        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'text';
        context.entity                   = this.CollectionEvent;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.createDirective          = createDirective;
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = faker.random.word();
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a date time annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.DATE_TIME()),
            newValue = faker.date.recent(10);

        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'dateTime';
        context.entity                   = this.CollectionEvent;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.createDirective          = createDirective;
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = { date: newValue, time: newValue };
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a number annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.NUMBER());

        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'number';
        context.entity                   = this.CollectionEvent;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.createDirective          = createDirective;
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = 10;
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a number annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.NUMBER());

        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'number';
        context.entity                   = this.CollectionEvent;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.createDirective          = createDirective;
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = 1;
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a single select annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var collectionEvent = this.collectionEventWithAnnotation(
          this.AnnotationValueType.SELECT(), 1);

        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'select';
        context.entity                   = this.CollectionEvent;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.createDirective          = createDirective;
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = collectionEvent.annotations[0].annotationType.options[0];
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a multiple select annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var collectionEvent = this.collectionEventWithAnnotation(
          this.AnnotationValueType.SELECT(), 2);

        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'selectMultiple';
        context.entity                   = this.CollectionEvent;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.createDirective          = createDirective;
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = collectionEvent.annotations[0].annotationType.options;
      }));

      annotationUpdateSharedBehaviour(context);

    });

  });

  function sharedUpdateBehaviour(context) {

    describe('(shared) tests', function() {

      beforeEach(inject(function() {
        this.CollectionEvent      = this.$injector.get('CollectionEvent');
        this.modalInput           = this.$injector.get('modalInput');
        this.notificationsService = this.$injector.get('notificationsService');
      }));


      it('on update should invoke the update method on entity', function() {
        var directive,
            deferred = this.$q.defer();

        deferred.resolve(context.newValue);

        spyOn(this.modalInput, context.modalInputFuncName)
          .and.returnValue({ result: deferred.promise});
        spyOn(this.CollectionEvent.prototype, context.ceventUpdateFuncName)
          .and.returnValue(this.$q.when(context.collectionEvent));
        spyOn(this.notificationsService, 'success').and.returnValue(this.$q.when('OK'));

        directive = createDirective(this);
        directive.controller[context.controllerUpdateFuncName]();
        directive.scope.$digest();

        expect(this.CollectionEvent.prototype[context.ceventUpdateFuncName]).toHaveBeenCalled();
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
        spyOn(this.CollectionEvent.prototype, context.ceventUpdateFuncName)
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
