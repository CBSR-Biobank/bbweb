/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import faker  from 'faker';
import ngModule from '../../index'
import sharedBehaviour from '../../../test/behaviours/annotationUpdateSharedBehaviour';

describe('Component: ceventView', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

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

      this.collectionEventWithAnnotation = (valueType, maxValueCount = 0) => {
        const jsonAnnotationType  = this.Factory.annotationType({ valueType: valueType,
                                                                  maxValueCount: maxValueCount }),
              jsonCeventType      = this.Factory.collectionEventType({ annotationTypes: [ jsonAnnotationType ]}),
              value               = this.Factory.valueForAnnotation(jsonAnnotationType),
              jsonAnnotation      = this.Factory.annotation({ value: value }, jsonAnnotationType),
              jsonCevent          = this.Factory.collectionEvent({ collectionEvent: jsonCeventType,
                                                                   annotations: [ jsonAnnotation ]});

        return this.CollectionEvent.create(jsonCevent);
      };

      this.createController = (study, participant, collectionEventType, collectionEvent) => {
        expect(collectionEventType).toBeDefined();
        expect(collectionEventType).not.toBeArray();
        expect(collectionEvent).toBeDefined();

        if (collectionEvent === undefined) {
          console.log('here')
        }

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
    const collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.SELECT,
                                                               this.AnnotationMaxValueCount.SELECT_MULTIPLE),
          collectionEventType = collectionEvent.collectionEventType,
          participant = this.Participant.create(this.Factory.participant()),
          study = this.Study.create(this.Factory.defaultStudy());

    this.createController(study, participant, collectionEventType, collectionEvent);

    expect(this.controller.collectionEventType).toBe(collectionEventType);
    expect(this.controller.collectionEvent).toBe(collectionEvent);
    expect(this.controller.panelOpen).toBeTrue();

    expect(this.controller.editTimeCompleted).toBeFunction();
    expect(this.controller.editAnnotation).toBeFunction();
    expect(this.controller.panelButtonClicked).toBeFunction();
  });

  it('panel can be closed and opened', function() {
    const collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.SELECT,
                                                               this.AnnotationMaxValueCount.SELECT_MULTIPLE),
          collectionEventType = collectionEvent.collectionEventType,
          participant = this.Participant.create(this.Factory.participant()),
          study = this.Study.create(this.Factory.defaultStudy());

    this.createController(study, participant, collectionEventType, collectionEvent);
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
        const collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.TEXT),
              study           = this.Study.create(this.Factory.defaultStudy()),
              participant     = this.Participant.create(this.Factory.participant());

        context.entityInstance           = collectionEvent;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'text';
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = faker.random.word();

        context.createController = () =>
          this.createController(study, participant, collectionEvent.collectionEventType, collectionEvent);
      });

      sharedBehaviour(context);

    });

    describe('updates to a date time annotation', function () {

      beforeEach(function () {
        const newValue        = faker.date.recent(10),
              collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.DATE_TIME),
              study           = this.Study.create(this.Factory.defaultStudy()),
              participant     = this.Participant.create(this.Factory.participant());

        context.entityInstance           = collectionEvent;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'dateTime';
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = { date: newValue, time: newValue };

        context.createController = () =>
          this.createController(study, participant, collectionEvent.collectionEventType, collectionEvent);
      });

      sharedBehaviour(context);

    });

    describe('updates to a number annotation', function () {

      beforeEach(function () {
        const collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.NUMBER),
              study           = this.Study.create(this.Factory.defaultStudy()),
              participant     = this.Participant.create(this.Factory.participant());

        context.entityInstance           = collectionEvent;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'number';
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = 10;

        context.createController = () =>
          this.createController(study, participant, collectionEvent.collectionEventType, collectionEvent);
      });

      sharedBehaviour(context);

    });

    describe('updates to a single select annotation', function () {

      beforeEach(function () {
        const collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.SELECT,
                                                                   this.AnnotationMaxValueCount.SELECT_SINGLE),
              study           = this.Study.create(this.Factory.defaultStudy()),
              participant     = this.Participant.create(this.Factory.participant());

        context.entityInstance           = collectionEvent;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'select';
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = collectionEvent.annotations[0].annotationType.options[0];

        context.createController = () =>
          this.createController(study, participant, collectionEvent.collectionEventType, collectionEvent);
      });

      sharedBehaviour(context);

    });

    describe('updates to a multiple select annotation', function () {

      beforeEach(function () {
        const collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.SELECT,
                                                                   this.AnnotationMaxValueCount.SELECT_MULTIPLE),
              study           = this.Study.create(this.Factory.defaultStudy()),
              participant     = this.Participant.create(this.Factory.participant());

        context.entityInstance           = collectionEvent;
        context.controllerUpdateFuncName = 'editAnnotation';
        context.modalInputFuncName       = 'selectMultiple';
        context.annotation               = collectionEvent.annotations[0];
        context.newValue                 = collectionEvent.annotations[0].annotationType.options;

        context.createController = () =>
          this.createController(study, participant, collectionEvent.collectionEventType, collectionEvent);
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
        this.collectionEvent = this.collectionEventWithAnnotation(
          this.AnnotationValueType.SELECT,
          this.AnnotationMaxValueCount.SELECT_MULTIPLE);

        this.study = this.Study.create(this.Factory.defaultStudy());

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
      this.collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.NUMBER);
      this.study = this.Study.create(this.Factory.defaultStudy());
    });

    it('can remove the collection event when cevent has no specimens', function() {
      const collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.SELECT,
                                                                 this.AnnotationMaxValueCount.SELECT_MULTIPLE),
            collectionEventType = collectionEvent.collectionEventType;

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

      this.createController(this.study, this.participant, collectionEventType, collectionEvent);
      this.controller.remove();
      this.scope.$digest();
      expect(this.CollectionEvent.prototype.remove).toHaveBeenCalled();
      expect(this.notificationsService.success).toHaveBeenCalled();
      expect(this.modalService.modalOkCancel.calls.count()).toBe(1);
    });

    it('cannot remove the collection event due to server error', function() {
      const collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.SELECT,
                                                                 this.AnnotationMaxValueCount.SELECT_MULTIPLE),
            collectionEventType = collectionEvent.collectionEventType;

      this.createController(this.study, this.participant, collectionEventType, collectionEvent);

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
      const collectionEvent = this.collectionEventWithAnnotation(this.AnnotationValueType.SELECT,
                                                                 this.AnnotationMaxValueCount.SELECT_MULTIPLE),
            collectionEventType = collectionEvent.collectionEventType,
            specimen = new this.Specimen(this.Factory.specimen());

      this.Specimen.list =
        jasmine.createSpy('list').and.returnValue(this.$q.when({ items: [ specimen ] }));

      this.modalService.modalOk =
        jasmine.createSpy('modalOk').and.returnValue(this.$q.when('OK'));

      this.createController(this.study, this.participant, collectionEventType, collectionEvent);
      this.controller.remove();
      this.scope.$digest();
      expect(this.modalService.modalOk).toHaveBeenCalled();
    });

  });

  xit('should allow to edit  the visit type', function() {
    fail('this test should be written when the functionality is implemented');
  });

});
