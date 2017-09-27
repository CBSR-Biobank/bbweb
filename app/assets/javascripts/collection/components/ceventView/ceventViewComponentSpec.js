/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import faker  from 'faker';
import sharedBehaviour from '../../../test/annotationUpdateSharedBehaviour';

describe('Component: ceventView', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin.prototype);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'Study',
                              'Participant',
                              'CollectionEvent',
                              'CollectionEventType',
                              'AnnotationValueType',
                              'AnnotationMaxValueCount',
                              'Specimen',
                              'domainNotificationService',
                              'modalService',
                              'notificationsService',
                              'factory');

      this.jsonCevent      = this.factory.collectionEvent();
      this.jsonParticipant = this.factory.defaultParticipant();
      this.jsonCeventType  = this.factory.defaultCollectionEventType();

      this.participant     = new this.Participant(this.jsonParticipant);
      this.collectionEvent = new this.CollectionEvent(this.jsonCevent);
      this.pagedResult     = this.factory.pagedResult([ this.collectionEvent ]);

      this.collectionEventWithAnnotation = (valueType, maxValueCount) => {
        var jsonAnnotationType,
            value,
            jsonAnnotation,
            jsonCeventType,
            jsonCevent;

        maxValueCount = maxValueCount || 0;

        jsonAnnotationType  = this.factory.annotationType({ valueType: valueType,
                                                            maxValueCount: maxValueCount });
        jsonCeventType      = this.factory.collectionEventType({ annotationTypes: [ jsonAnnotationType ]});
        value               = this.factory.valueForAnnotation(jsonAnnotationType);
        jsonAnnotation      = this.factory.annotation({ value: value }, jsonAnnotationType);
        jsonCevent          = this.factory.collectionEvent({ collectionEvent: jsonCeventType,
                                                             annotations: [ jsonAnnotation ]});
        return this.CollectionEvent.create(jsonCevent);
      };

      this.createController = (study, collectionEventTypes, collectionEvent) => {
        if (_.isUndefined(collectionEventTypes)) {
          fail('collectionEventTypes is undefined');
        }

        if (_.isUndefined(collectionEvent)) {
          fail('collectionEvent is undefined');
        }

        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          `<cevent-view
             study="vm.study"
             collection-event-types="vm.collectionEventTypes"
             collection-event="vm.collectionEvent">
           </cevent-view>`,
          {
            study:                study,
            collectionEvent:      collectionEvent,
            collectionEventTypes: collectionEventTypes
          },
          'ceventView');
      };
    });
  });

  it('has valid scope', function() {
    var collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.SELECT,
                                                             this.AnnotationMaxValueCount.SELECT_MULTIPLE),
        collectionEventTypes = [ collectionEvent.collectionEventType ],
        study = new this.Study(this.factory.defaultStudy());

    this.createController(study, collectionEventTypes, collectionEvent);

    expect(this.controller.collectionEventTypes).toBe(collectionEventTypes);
    expect(this.controller.collectionEvent).toBe(collectionEvent);
    expect(this.controller.panelOpen).toBeTrue();

    expect(this.controller.editTimeCompleted).toBeFunction();
    expect(this.controller.editAnnotation).toBeFunction();
    expect(this.controller.panelButtonClicked).toBeFunction();
  });

  it('panel can be closed and opened', function() {
    var collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.SELECT,
                                                             this.AnnotationMaxValueCount.SELECT_MULTIPLE),
        collectionEventTypes = [ collectionEvent.collectionEventType ],
        study = new this.Study(this.factory.defaultStudy());

    this.createController(study, collectionEventTypes, collectionEvent);
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
      context.controllerUpdateFuncName = 'editTimeCompleted';
      context.modalInputFuncName       = 'dateTime';
      context.ceventUpdateFuncName     = 'updateTimeCompleted';
      context.collectionEvent          = this.collectionEvent;
      context.newValue                 = faker.date.recent(10);
    }));

    sharedUpdateBehaviour(context);

  });

  describe('updates to annotations', function () {

    var context = {};

    beforeEach(inject(function () {
      context.entity                   = this.CollectionEvent;
      context.entityUpdateFuncName     = 'addAnnotation';
    }));

    describe('updates to a text annotation', function () {

      beforeEach(inject(function () {
        var self = this,
            collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.TEXT),
            study = new this.Study(this.factory.defaultStudy());

        context.entityInstance           = collectionEvent;
        context.createController         = createController;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'text';
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = faker.random.word();

        function createController() {
          return self.createController(study, [ collectionEvent.collectionEventType ], collectionEvent);
        }
      }));

      sharedBehaviour(context);

    });

    describe('updates to a date time annotation', function () {

      beforeEach(inject(function () {
        var self = this,
            newValue = faker.date.recent(10),
            collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.DATE_TIME),
            study = new this.Study(this.factory.defaultStudy());

        context.entityInstance           = collectionEvent;
        context.createController         = createController;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'dateTime';
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = { date: newValue, time: newValue };

        function createController() {
          return self.createController(study, [ collectionEvent.collectionEventType ], collectionEvent);
        }
      }));

      sharedBehaviour(context);

    });

    describe('updates to a number annotation', function () {

      beforeEach(inject(function () {
        var self = this,
            collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.NUMBER),
            study = new this.Study(this.factory.defaultStudy());

        context.entityInstance           = collectionEvent;
        context.createController          = createController;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'number';
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = 10;

        function createController() {
          return self.createController(study, [ collectionEvent.collectionEventType ], collectionEvent);
        }
      }));

      sharedBehaviour(context);

    });

    describe('updates to a single select annotation', function () {

      beforeEach(inject(function () {
        var self = this,
            collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.SELECT,
                                                                 this.AnnotationMaxValueCount.SELECT_SINGLE),
            study = new this.Study(this.factory.defaultStudy());

        context.entityInstance           = collectionEvent;
        context.createController         = createController;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'select';
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = collectionEvent.annotations[0].annotationType.options[0];

        function createController() {
          return self.createController(study, [ collectionEvent.collectionEventType ], collectionEvent);
        }
      }));

      sharedBehaviour(context);

    });

    describe('updates to a multiple select annotation', function () {

      beforeEach(inject(function () {
        var self = this,
            collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.SELECT,
                                                                 this.AnnotationMaxValueCount.SELECT_MULTIPLE),
            study = new this.Study(this.factory.defaultStudy());

        context.entityInstance           = collectionEvent;
        context.createController          = createController;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'selectMultiple';
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = collectionEvent.annotations[0].annotationType.options;

        function createController() {
          return self.createController(study, [ collectionEvent.collectionEventType ], collectionEvent);
        }
      }));

      sharedBehaviour(context);

    });

  });

  function sharedUpdateBehaviour(context) {

    describe('(shared) tests', function() {

      beforeEach(inject(function() {
        this.injectDependencies('CollectionEvent',
                                'modalInput',
                                'notificationsService');
        this.collectionEvent = this.collectionEventWithAnnotation(
          this.AnnotationValueType.SELECT,
          this.AnnotationMaxValueCount.SELECT_MULTIPLE);

        this.study = new this.Study(this.factory.defaultStudy());

      }));


      it('on update should invoke the update method on entity', function() {
        spyOn(this.modalInput, context.modalInputFuncName)
          .and.returnValue({ result: this.$q.when(context.newValue )});
        spyOn(this.CollectionEvent.prototype, context.ceventUpdateFuncName)
          .and.returnValue(this.$q.when(context.collectionEvent));
        spyOn(this.notificationsService, 'success').and.returnValue(this.$q.when('OK'));

        this.createController(this.study,
                              [ this.collectionEvent.collectionEventType ],
                              this.collectionEvent);
        this.controller[context.controllerUpdateFuncName]();
        this.scope.$digest();

        expect(this.CollectionEvent.prototype[context.ceventUpdateFuncName]).toHaveBeenCalled();
        expect(this.notificationsService.success).toHaveBeenCalled();
      });

      it('error message should be displayed when update fails', function() {
        this.createController(this.study,
                              [ this.collectionEvent.collectionEventType ],
                              this.collectionEvent);
        spyOn(this.modalInput, context.modalInputFuncName)
          .and.returnValue({ result: this.$q.when(context.newValue )});
        spyOn(this.CollectionEvent.prototype, context.ceventUpdateFuncName)
          .and.returnValue(this.$q.reject('simulated error'));
        spyOn(this.notificationsService, 'updateError').and.returnValue(this.$q.when('OK'));

        this.controller[context.controllerUpdateFuncName]();
        this.scope.$digest();

        expect(this.notificationsService.updateError).toHaveBeenCalled();
      });

    });
  }

  describe('when removing a collection event', function() {

    beforeEach(function() {
      this.collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.NUMBER);
      this.study = new this.Study(this.factory.defaultStudy());
    });

    it('can remove the collection event when cevent has no specimens', function() {
      var collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.SELECT,
                                                               this.AnnotationMaxValueCount.SELECT_MULTIPLE),
          collectionEventTypes = [ collectionEvent.collectionEventType ];

      this.Specimen.list =
        jasmine.createSpy('list').and.returnValue(this.$q.when({ items: [] }));

      this.modalService.modalOkCancel =
        jasmine.createSpy('modalOkCancel').and.returnValue(this.$q.when('OK'));

      this.CollectionEvent.prototype.remove =
        jasmine.createSpy('remove').and.returnValue(this.$q.when(this.collectionEvent));

      this.notificationsService.success =
        jasmine.createSpy('remove').and.returnValue(this.$q.when(null));

      this.$state.go =
        jasmine.createSpy('state.go').and.returnValue(null);

      this.createController(this.study, collectionEventTypes, collectionEvent);
      this.controller.remove();
      this.scope.$digest();
      expect(this.CollectionEvent.prototype.remove).toHaveBeenCalled();
      expect(this.notificationsService.success).toHaveBeenCalled();
      expect(this.modalService.modalOkCancel.calls.count()).toBe(1);
    });

    it('cannot remove the collection event due to server error', function() {
      var collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.SELECT,
                                                               this.AnnotationMaxValueCount.SELECT_MULTIPLE),
          collectionEventTypes = [ collectionEvent.collectionEventType ];

      this.createController(this.study, collectionEventTypes, collectionEvent);

      this.Specimen.list =
        jasmine.createSpy('list').and.returnValue(this.$q.when({ items: [] }));

      this.modalService.modalOkCancel =
        jasmine.createSpy('modalOkCancel').and.returnValue(this.$q.when('OK'));

      this.CollectionEvent.prototype.remove =
        jasmine.createSpy('remove').and.returnValue(this.$q.reject('simulated error'));

      this.notificationsService.success =
        jasmine.createSpy('remove').and.returnValue(this.$q.when(null));

      this.$state.go =
        jasmine.createSpy('state.go').and.returnValue(null);

      this.controller.remove();
      this.scope.$digest();
      expect(this.CollectionEvent.prototype.remove).toHaveBeenCalled();
      expect(this.notificationsService.success).not.toHaveBeenCalled();
      expect(this.modalService.modalOkCancel.calls.count()).toBe(2);
    });

    it('can NOT remove the collection event when cevent HAS specimens', function() {
      var collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.SELECT,
                                                               this.AnnotationMaxValueCount.SELECT_MULTIPLE),
          collectionEventTypes = [ collectionEvent.collectionEventType ],
          specimen = new this.Specimen(this.factory.specimen());

      this.Specimen.list =
        jasmine.createSpy('list').and.returnValue(this.$q.when({ items: [ specimen ] }));

      this.modalService.modalOk =
        jasmine.createSpy('modalOk').and.returnValue(this.$q.when('OK'));

      this.createController(this.study, collectionEventTypes, collectionEvent);
      this.controller.remove();
      this.scope.$digest();
      expect(this.modalService.modalOk).toHaveBeenCalled();
    });

  });

  xit('should allow to edit  the visit type', function() {
    fail('this test should be written when the functionality is implemented');
  });

});
