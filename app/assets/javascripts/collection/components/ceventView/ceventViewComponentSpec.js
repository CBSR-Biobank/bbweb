/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import faker  from 'faker';
import ngModule from '../../index'
import sharedBehaviour from 'test/behaviours/annotationUpdateSharedBehaviour';

describe('Component: ceventView', function() {

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
                              'CollectionEvent',
                              'CollectionEventType',
                              'AnnotationValueType',
                              'AnnotationMaxValueCount',
                              'Specimen',
                              'domainNotificationService',
                              'modalService',
                              'notificationsService',
                              'Factory');

      this.jsonCevent      = this.Factory.collectionEvent();
      this.jsonParticipant = this.Factory.defaultParticipant();
      this.jsonCeventType  = this.Factory.defaultCollectionEventType();

      this.participant     = new this.Participant(this.jsonParticipant);
      this.collectionEvent = new this.CollectionEvent(this.jsonCevent);
      this.pagedResult     = this.Factory.pagedResult([ this.collectionEvent ]);

      this.fixture = (valueType, maxValueCount = 0) => {
        const jsonAnnotationType  = this.Factory.annotationType({ valueType: valueType,
                                                                  maxValueCount: maxValueCount }),
              jsonCeventType      = this.Factory.collectionEventType({ annotationTypes: [ jsonAnnotationType ]}),
              value               = this.Factory.valueForAnnotation(jsonAnnotationType),
              jsonAnnotation      = this.Factory.annotation({ value: value }, jsonAnnotationType),
              jsonCevent          = this.Factory.collectionEvent({ collectionEvent: jsonCeventType,
                                                                   annotations: [ jsonAnnotation ]});

        return {
          study:               this.Study.create(this.Factory.defaultStudy()),
          participant:         this.Participant.create(this.Factory.participant()),
          collectionEventType: this.CollectionEventType.create(jsonCeventType),
          collectionEvent:     this.CollectionEvent.create(Object.assign(
            jsonCevent, { collectionEventType: jsonCeventType }))
        };
      };

      this.createController = (study, participant, collectionEventType, collectionEvent) => {
        expect(collectionEventType).toBeDefined();
        expect(collectionEventType).not.toBeArray();
        expect(collectionEvent).toBeDefined();

        this.CollectionEventType.get = jasmine.createSpy().and.returnValue(this.$q.when(collectionEventType));

        ComponentTestSuiteMixin.createController.call(
          this,
          `<cevent-view study="vm.study"
                        participant="vm.participant"
                        collection-event-type="vm.collectionEventType"
                        collection-event="vm.collectionEvent">
           </cevent-view>`,
          {
            study,
            participant,
            collectionEvent,
            collectionEventType
          },
          'ceventView');
      };
    });
  });

  it('has valid scope', function() {
    const f = this.fixture(this.AnnotationValueType.SELECT,
                           this.AnnotationMaxValueCount.SELECT_MULTIPLE);

    this.createController(f.study, f.participant, f.collectionEventType, f.collectionEvent);

    expect(this.controller.collectionEventType).toBe(f.collectionEventType);
    expect(this.controller.collectionEvent).toBe(f.collectionEvent);
    expect(this.controller.panelOpen).toBeTrue();

    expect(this.controller.editTimeCompleted).toBeFunction();
    expect(this.controller.editAnnotation).toBeFunction();
    expect(this.controller.panelButtonClicked).toBeFunction();
  });

  it('panel can be closed and opened', function() {
    const f = this.fixture(this.AnnotationValueType.SELECT,
                           this.AnnotationMaxValueCount.SELECT_MULTIPLE);

    this.createController(f.study, f.participant, f.collectionEventType, f.collectionEvent);
    this.controller.panelButtonClicked();
    this.scope.$digest();
    expect(this.controller.panelOpen).toBeFalse();

    this.controller.panelButtonClicked();
    this.scope.$digest();
    expect(this.controller.panelOpen).toBeTrue();
  });

  describe('updates to time completed', function () {

    const context = {};

    beforeEach(function () {
      context.controllerUpdateFuncName = 'editTimeCompleted';
      context.modalInputFuncName       = 'dateTime';
      context.ceventUpdateFuncName     = 'updateTimeCompleted';
      context.collectionEvent          = this.collectionEvent;
      context.newValue                 = faker.date.recent(10);
    });

    sharedUpdateBehaviour(context);

  });

  describe('updates to annotations', function () {

    const context = {};

    beforeEach(function () {
      context.entity                   = this.CollectionEvent;
      context.entityUpdateFuncName     = 'addAnnotation';
    });

    describe('updates to a text annotation', function () {

      beforeEach(function () {
        const f = this.fixture(this.AnnotationValueType.TEXT);
        context.entityInstance           = f.collectionEvent;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'text';
        context.annotation               = f.collectionEvent.annotations[0];
        context.newValue                 = faker.random.word();
        context.createController = () =>
          this.createController(f.study, f.participant, f.collectionEventType, f.collectionEvent);
      });

      sharedBehaviour(context);

    });

    describe('updates to a date time annotation', function () {

      beforeEach(function () {
        const f = this.fixture(this.AnnotationValueType.DATE_TIME);
        const newValue = faker.date.recent(10);
        context.entityInstance           = f.collectionEvent;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'dateTime';
        context.annotation               = f.collectionEvent.annotations[0];
        context.newValue                 = { date: newValue, time: newValue };
        context.createController = () =>
          this.createController(f.study, f.participant, f.collectionEventType, f.collectionEvent);
      });

      sharedBehaviour(context);

    });

    describe('updates to a number annotation', function () {

      beforeEach(function () {
        const f = this.fixture(this.AnnotationValueType.NUMBER);
        context.entityInstance           = f.collectionEvent;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'number';
        context.annotation               = f.collectionEvent.annotations[0];
        context.newValue                 = 10;
        context.createController = () =>
          this.createController(f.study, f.participant, f.collectionEventType, f.collectionEvent);
      });

      sharedBehaviour(context);

    });

    describe('updates to a single select annotation', function () {

      beforeEach(function () {
        const f = this.fixture(this.AnnotationValueType.SELECT,
                           this.AnnotationMaxValueCount.SELECT_SINGLE);
        context.entityInstance           = f.collectionEvent;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'select';
        context.annotation               = f.collectionEvent.annotations[0];
        context.newValue                 = f.collectionEvent.annotations[0].annotationType.options[0];
        context.createController = () =>
          this.createController(f.study, f.participant, f.collectionEventType, f.collectionEvent);
      });

      sharedBehaviour(context);

    });

    describe('updates to a multiple select annotation', function () {

      beforeEach(function () {
        const f = this.fixture(this.AnnotationValueType.SELECT,
                           this.AnnotationMaxValueCount.SELECT_MULTIPLE);
        context.entityInstance           = f.collectionEvent;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'selectMultiple';
        context.annotation               = f.collectionEvent.annotations[0];
        context.newValue                 = f.collectionEvent.annotations[0].annotationType.options;
        context.createController = () =>
          this.createController(f.study, f.participant, f.collectionEventType, f.collectionEvent);
      });

      sharedBehaviour(context);

    });

  });

  function sharedUpdateBehaviour(context) {

    describe('(shared) tests', function() {

      beforeEach(function() {
        this.injectDependencies('CollectionEvent',
                                'modalInput',
                                'notificationsService');

        const f = this.fixture(this.AnnotationValueType.SELECT,
                               this.AnnotationMaxValueCount.SELECT_MULTIPLE);

        this.collectionEvent = f.collectionEvent;
        this.study = f.study;
      });


      it('on update should invoke the update method on entity', function() {
        spyOn(this.modalInput, context.modalInputFuncName)
          .and.returnValue({ result: this.$q.when(context.newValue )});
        spyOn(this.CollectionEvent.prototype, context.ceventUpdateFuncName)
          .and.returnValue(this.$q.when(context.collectionEvent));
        spyOn(this.notificationsService, 'success').and.returnValue(this.$q.when('OK'));

        this.createController(this.study,
                              this.participant,
                              this.collectionEvent.collectionEventType,
                              this.collectionEvent);
        this.controller[context.controllerUpdateFuncName]();
        this.scope.$digest();

        expect(this.CollectionEvent.prototype[context.ceventUpdateFuncName]).toHaveBeenCalled();
        expect(this.notificationsService.success).toHaveBeenCalled();
      });

      it('error message should be displayed when update fails', function() {
        this.createController(this.study,
                              this.participant,
                              this.collectionEvent.collectionEventType,
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
      const f = this.fixture(this.AnnotationValueType.SELECT,
                             this.AnnotationMaxValueCount.SELECT_MULTIPLE);

      this.collectionEvent = f.collectionEvent;
      this.study = f.study;
    });

    it('can remove the collection event when cevent has no specimens', function() {
      const f = this.fixture(this.AnnotationValueType.SELECT,
                             this.AnnotationMaxValueCount.SELECT_MULTIPLE);

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

      this.createController(this.study, this.participant, f.collectionEventType, f.collectionEvent);
      this.controller.remove();
      this.scope.$digest();
      expect(this.CollectionEvent.prototype.remove).toHaveBeenCalled();
      expect(this.notificationsService.success).toHaveBeenCalled();
      expect(this.modalService.modalOkCancel.calls.count()).toBe(1);
    });

    it('cannot remove the collection event due to server error', function() {
      const f = this.fixture(this.AnnotationValueType.SELECT,
                             this.AnnotationMaxValueCount.SELECT_MULTIPLE);

      this.createController(this.study, this.participant, f.collectionEventType, f.collectionEvent);

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
      const f = this.fixture(this.AnnotationValueType.SELECT,
                             this.AnnotationMaxValueCount.SELECT_MULTIPLE);
      const specimen = new this.Specimen(this.Factory.specimen());

      this.Specimen.list =
        jasmine.createSpy('list').and.returnValue(this.$q.when({ items: [ specimen ] }));

      this.modalService.modalOk =
        jasmine.createSpy('modalOk').and.returnValue(this.$q.when('OK'));

      this.createController(this.study, this.participant, f.collectionEventType, f.collectionEvent);
      this.controller.remove();
      this.scope.$digest();
      expect(this.modalService.modalOk).toHaveBeenCalled();
    });

  });

  xit('should allow to edit  the visit type', function() {
    fail('this test should be written when the functionality is implemented');
  });

});
