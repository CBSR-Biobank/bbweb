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

  describe('directive: ceventViewDirective', function() {

    var collectionEventWithAnnotation = function (valueType, maxValueCount) {
      var jsonAnnotationType,
          value,
          jsonAnnotation,
          jsonCeventType,
          jsonCevent,
          collectionEventType;

      maxValueCount = maxValueCount || 0;

      jsonAnnotationType = this.factory.annotationType({ valueType: valueType,
                                                         maxValueCount: maxValueCount });
      value              = this.factory.valueForAnnotation(jsonAnnotationType);
      jsonAnnotation     = this.factory.annotation({ value: value }, jsonAnnotationType);
      jsonCeventType     = this.factory.collectionEventType({ annotationTypes: [ jsonAnnotationType ]});
      jsonCevent         = this.factory.collectionEvent({ annotations: [ jsonAnnotation ]});
      collectionEventType = new this.CollectionEventType(jsonCeventType);
      return new this.CollectionEvent(jsonCevent, collectionEventType);
    };

    var createDirective = function (collectionEventTypes, collectionEvent) {
      collectionEventTypes = collectionEventTypes || this.collectionEventTypes;
      collectionEvent = collectionEvent || this.collectionEvent;

      this.element = angular.element([
        '<cevent-view',
        '  collection-event-types="vm.collectionEventTypes"',
        '  collection-event="vm.collectionEvent">',
        '</cevent-view>'
      ].join(''));

      this.scope = this.$rootScope.$new();
      this.scope.vm = {
        collectionEventTypes: collectionEventTypes,
        collectionEvent:      collectionEvent
      };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('ceventView');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'Participant',
                              'CollectionEvent',
                              'CollectionEventType',
                              'AnnotationValueType',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/ceventView/ceventView.html',
        '/assets/javascripts/collection/components/ceventSpecimensView/ceventSpecimensView.html',
        '/assets/javascripts/admin/directives/statusLine/statusLine.html',
        '/assets/javascripts/common/directives/pagination.html');

      self.jsonCevent      = self.factory.collectionEvent();
      self.jsonParticipant = self.factory.defaultParticipant();
      self.jsonCeventType  = self.factory.defaultCollectionEventType();

      self.participant          = new self.Participant(self.jsonParticipant);
      self.collectionEvent      = new self.CollectionEvent(self.jsonCevent);
      self.pagedResult          = self.factory.pagedResult([ self.collectionEvent ]);
      self.collectionEventTypes = [ new self.CollectionEventType(self.jsonCeventType) ];
    }));

    it('has valid scope', function() {
      createDirective.call(this);

      expect(this.controller.collectionEventTypes).toBe(this.collectionEventTypes);
      expect(this.controller.collectionEvent).toBe(this.collectionEvent);
      expect(this.controller.panelOpen).toBeTrue();
      expect(this.controller.timeCompletedLocal).toBeDefined();

      expect(this.controller.editVisitType).toBeFunction();
      expect(this.controller.editTimeCompleted).toBeFunction();
      expect(this.controller.editAnnotation).toBeFunction();
      expect(this.controller.panelButtonClicked).toBeFunction();
    });

    it('panel can be closed and opened', function() {
      createDirective.call(this);

      this.controller.panelButtonClicked();
      this.scope.$digest();
      expect(this.controller.panelOpen).toBeFalse();

      this.controller.panelButtonClicked();
      this.scope.$digest();
      expect(this.controller.panelOpen).toBeTrue();
    });

    describe('updates to time completed', function () {

      var context = {};

      beforeEach(inject(function () {
        context.createDirective          = createDirective;
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
        var collectionEvent = collectionEventWithAnnotation.call(this, this.AnnotationValueType.TEXT);

        context.createDirective          = createDirective;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'text';
        context.entity                   = this.CollectionEvent;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = faker.random.word();
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a date time annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var collectionEvent = collectionEventWithAnnotation.call(this, this.AnnotationValueType.DATE_TIME),
            newValue = faker.date.recent(10);

        context.createDirective          = createDirective;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'dateTime';
        context.entity                   = this.CollectionEvent;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = { date: newValue, time: newValue };
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a number annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var collectionEvent = collectionEventWithAnnotation.call(this, this.AnnotationValueType.NUMBER);

        context.createDirective          = createDirective;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'number';
        context.entity                   = this.CollectionEvent;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = 10;
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a number annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var collectionEvent = collectionEventWithAnnotation.call(this, this.AnnotationValueType.NUMBER);

        context.createDirective          = createDirective;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'number';
        context.entity                   = this.CollectionEvent;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = 1;
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a single select annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var collectionEvent = collectionEventWithAnnotation.call(this,
                                                                 this.AnnotationValueType.SELECT,
                                                                 1);

        context.createDirective          = createDirective;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'select';
        context.entity                   = this.CollectionEvent;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = collectionEvent.annotations[0].annotationType.options[0];
      }));

      annotationUpdateSharedBehaviour(context);

    });

    describe('updates to a multiple select annotation', function () {

      var context = {};

      beforeEach(inject(function () {
        var collectionEvent = collectionEventWithAnnotation.call(this,
                                                                 this.AnnotationValueType.SELECT,
                                                                 2);

        context.createDirective          = createDirective;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'selectMultiple';
        context.entity                   = this.CollectionEvent;
        context.entityUpdateFuncName     = 'addAnnotation';
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = collectionEvent.annotations[0].annotationType.options;
      }));

      annotationUpdateSharedBehaviour(context);

    });

    function sharedUpdateBehaviour(context) {

      describe('(shared) tests', function() {

        beforeEach(inject(function() {

          this.injectDependencies('CollectionEvent',
                                  'modalInput',
                                  'notificationsService');
        }));


        it('on update should invoke the update method on entity', function() {
          spyOn(this.modalInput, context.modalInputFuncName)
            .and.returnValue({ result: this.$q.when(context.newValue )});
          spyOn(this.CollectionEvent.prototype, context.ceventUpdateFuncName)
            .and.returnValue(this.$q.when(context.collectionEvent));
          spyOn(this.notificationsService, 'success').and.returnValue(this.$q.when('OK'));

          createDirective.call(this);
          this.controller[context.controllerUpdateFuncName]();
          this.scope.$digest();

          expect(this.CollectionEvent.prototype[context.ceventUpdateFuncName]).toHaveBeenCalled();
          expect(this.notificationsService.success).toHaveBeenCalled();
        });

        it('error message should be displayed when update fails', function() {
          spyOn(this.modalInput, context.modalInputFuncName)
            .and.returnValue({ result: this.$q.when(context.newValue )});
          spyOn(this.CollectionEvent.prototype, context.ceventUpdateFuncName)
            .and.returnValue(this.$q.reject('simulated error'));
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
