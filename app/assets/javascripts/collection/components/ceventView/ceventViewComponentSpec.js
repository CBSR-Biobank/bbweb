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

      this.injectDependencies('$httpBackend',
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
                              'modalInput',
                              'Factory');

      this.createFixture = (valueType, maxValueCount = 0) => {
        const plainAnnotationType  = this.Factory.annotationType({
          valueType: valueType,
          maxValueCount: maxValueCount
        });
        const plainEventType      = this.Factory.collectionEventType({
          annotationTypes: [ plainAnnotationType ]
        });
        const value               = this.Factory.valueForAnnotation(plainAnnotationType);
        const plainAnnotation      = this.Factory.annotation({ value: value }, plainAnnotationType);
        const plainCevent          = this.Factory.collectionEvent({
          collectionEvent: plainEventType,
          annotations: [ plainAnnotation ]
        });

        return {
          study:               this.Study.create(this.Factory.defaultStudy()),
          participant:         this.Participant.create(this.Factory.participant()),
          plainEventType:      plainEventType,
          collectionEventType: this.CollectionEventType.create(plainEventType),
          collectionEvent:     this.CollectionEvent.create(Object.assign(
            plainCevent, { collectionEventType: plainEventType }))
        };
      };

      this.createController = (fixture) => {
        expect(fixture.collectionEventType).toBeDefined();
        expect(fixture.collectionEventType).not.toBeArray();
        expect(fixture.collectionEvent).toBeDefined();

        this.$httpBackend
          .expectGET(this.url('studies/cetypes', fixture.study.slug, fixture.collectionEventType.slug))
          .respond(fixture.plainEventType);

        this.createControllerInternal(
          `<cevent-view study="vm.study"
                        participant="vm.participant"
                        collection-event-type="vm.collectionEventType"
                        collection-event="vm.collectionEvent">
           </cevent-view>`,
          {
            study:               fixture.study,
            participant:         fixture.participant,
            collectionEvent:     fixture.collectionEvent,
            collectionEventType: fixture.collectionEventType
          },
          'ceventView');
        this.$httpBackend.flush();
      };
    });
  });

  it('has valid scope', function() {
    const f = this.createFixture(this.AnnotationValueType.SELECT,
                                 this.AnnotationMaxValueCount.SELECT_MULTIPLE);

    this.createController(f);

    expect(this.controller.collectionEventType).toEqual(f.collectionEventType);
    expect(this.controller.collectionEvent).toBe(f.collectionEvent);
    expect(this.controller.panelOpen).toBeTrue();

    expect(this.controller.editTimeCompleted).toBeFunction();
    expect(this.controller.editAnnotation).toBeFunction();
    expect(this.controller.panelButtonClicked).toBeFunction();
  });

  it('panel can be closed and opened', function() {
    const f = this.createFixture(this.AnnotationValueType.SELECT,
                                 this.AnnotationMaxValueCount.SELECT_MULTIPLE);

    this.createController(f);
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
        const f = this.createFixture(this.AnnotationValueType.TEXT);
        context.entityInstance           = f.collectionEvent;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'text';
        context.annotation               = f.collectionEvent.annotations[0];
        context.newValue                 = faker.random.word();
        context.createController = () =>
          this.createController(f);
      });

      sharedBehaviour(context);

    });

    describe('updates to a date time annotation', function () {

      beforeEach(function () {
        const f = this.createFixture(this.AnnotationValueType.DATE_TIME);
        const newValue = faker.date.recent(10);
        context.entityInstance           = f.collectionEvent;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'dateTime';
        context.annotation               = f.collectionEvent.annotations[0];
        context.newValue                 = newValue;
        context.createController = () =>
          this.createController(f);
      });

      sharedBehaviour(context);

    });

    describe('updates to a number annotation', function () {

      beforeEach(function () {
        const f = this.createFixture(this.AnnotationValueType.NUMBER);
        context.entityInstance           = f.collectionEvent;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'number';
        context.annotation               = f.collectionEvent.annotations[0];
        context.newValue                 = 10;
        context.createController = () =>
          this.createController(f);
      });

      sharedBehaviour(context);

    });

    describe('updates to a single select annotation', function () {

      beforeEach(function () {
        const f = this.createFixture(this.AnnotationValueType.SELECT,
                                     this.AnnotationMaxValueCount.SELECT_SINGLE);
        context.entityInstance           = f.collectionEvent;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'select';
        context.annotation               = f.collectionEvent.annotations[0];
        context.newValue                 = f.collectionEvent.annotations[0].annotationType.options[0];
        context.createController = () =>
          this.createController(f);
      });

      sharedBehaviour(context);

    });

    describe('updates to a multiple select annotation', function () {

      beforeEach(function () {
        const f = this.createFixture(this.AnnotationValueType.SELECT,
                                     this.AnnotationMaxValueCount.SELECT_MULTIPLE);
        context.entityInstance           = f.collectionEvent;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'selectMultiple';
        context.annotation               = f.collectionEvent.annotations[0];
        context.newValue                 = f.collectionEvent.annotations[0].annotationType.options;
        context.createController = () =>
          this.createController(f);
      });

      sharedBehaviour(context);

    });

  });

  function sharedUpdateBehaviour(context) {

    describe('(shared) tests', function() {

      beforeEach(function() {
        this.fixture = this.createFixture(this.AnnotationValueType.SELECT,
                                          this.AnnotationMaxValueCount.SELECT_MULTIPLE);
      });


      it('on update should invoke the update method on entity', function() {
        spyOn(this.modalInput, context.modalInputFuncName)
          .and.returnValue({ result: this.$q.when(context.newValue )});
        spyOn(this.CollectionEvent.prototype, context.ceventUpdateFuncName)
          .and.returnValue(this.$q.when(this.fixture.collectionEvent));
        spyOn(this.notificationsService, 'success').and.returnValue(this.$q.when('OK'));

        this.createController(this.fixture);
        this.controller[context.controllerUpdateFuncName]();
        this.scope.$digest();

        expect(this.CollectionEvent.prototype[context.ceventUpdateFuncName]).toHaveBeenCalled();
        expect(this.notificationsService.success).toHaveBeenCalled();
      });

      it('error message should be displayed when update fails', function() {
        this.createController(this.fixture);
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
      this.fixture = this.createFixture(this.AnnotationValueType.SELECT,
                                        this.AnnotationMaxValueCount.SELECT_MULTIPLE);
    });

    it('can remove the collection event when cevent has no specimens', function() {
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

      this.createController(this.fixture);
      this.controller.remove();
      this.scope.$digest();
      expect(this.CollectionEvent.prototype.remove).toHaveBeenCalled();
      expect(this.notificationsService.success).toHaveBeenCalled();
      expect(this.modalService.modalOkCancel.calls.count()).toBe(1);
    });

    it('cannot remove the collection event due to server error', function() {
      this.createController(this.fixture);

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
      const specimen = new this.Specimen(this.Factory.specimen());

      this.Specimen.list =
        jasmine.createSpy('list').and.returnValue(this.$q.when({ items: [ specimen ] }));

      this.modalService.modalOk =
        jasmine.createSpy('modalOk').and.returnValue(this.$q.when('OK'));

      this.createController(this.fixture);
      this.controller.remove();
      this.scope.$digest();
      expect(this.modalService.modalOk).toHaveBeenCalled();
    });

  });

  xit('should allow to edit  the visit type', function() {
    fail('this test should be written when the functionality is implemented');
  });

});
