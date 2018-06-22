/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../../../../app'
import sharedBehaviour from 'test/behaviours/annotationTypeViewComponentSharedBehaviour';

describe('Component: collectionEventAnnotationTypeView', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$httpBackend',
                              'notificationsService',
                              'Study',
                              'CollectionEventType',
                              'AnnotationType',
                              'modalService',
                              'notificationsService',
                              'Factory');

      this.init();
      this.url = (...paths) => {
        const args = [ 'studies/cetypes' ].concat(paths);
        return ComponentTestSuiteMixin.url(...args);
      };

      this.fixture = () => {
        const plainStudy          = this.Factory.study();
        const plainAnnotationType = this.Factory.annotationType();
        const plainEventType      = this.Factory.collectionEventType({
          annotationTypes: [ plainAnnotationType ]
        });
        const study               = this.Study.create(plainStudy);
        const collectionEventType = this.CollectionEventType.create(plainEventType);
        const annotationType      = collectionEventType.annotationTypes[0];

        return {
          plainStudy,
          study,
          plainEventType,
          collectionEventType,
          annotationType
        };
      };

      this.expectStudy = (plainStudy) => {
        if (!this.studyRequestHandler) {
          this.studyRequestHandler = this.$httpBackend
            .whenGET(new RegExp('^' + ComponentTestSuiteMixin.url('studies') + '/\\w+$'));
        }
        this.studyRequestHandler.respond(this.reply(plainStudy));
      }

      this.expectEventType = (plainEventType) => {
        if (!this.eventTypeRequestHandler) {
          this.eventTypeRequestHandler = this.$httpBackend
            .whenGET(new RegExp(this.url() + '/\\w+/\\w+$'))
        }
        this.eventTypeRequestHandler.respond(this.reply(plainEventType));
      };

      this.stateInit = (fixture) => {
        this.expectStudy(fixture.plainStudy);
        this.expectEventType(fixture.plainEventType);

        this.gotoUrl(
          `/admin/studies/${fixture.plainStudy.slug}/collection/events/${fixture.plainEventType.slug}/annottypes/${fixture.annotationType.slug}`);
        this.$httpBackend.flush();
        expect(this.$state.current.name)
          .toBe('home.admin.studies.study.collection.ceventType.annotationTypeView');
      };

      this.createController = (fixture) => {
        this.expectEventType(fixture.plainEventType);
        this.createControllerInternal(
          `<collection-event-annotation-type-view
              study="vm.study"
              collection-event-type="vm.collectionEventType"
              annotation-type="vm.annotationType"
           </collection-event-annotation-type-view>`,
          {
            study:               fixture.study,
            collectionEventType: fixture.collectionEventType,
            annotationType:      fixture.annotationType
          },
          'collectionEventAnnotationTypeView');
        this.$httpBackend.flush();
      };
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation()
    this.$httpBackend.verifyNoOutstandingRequest()
  });

  it('should have  valid scope', function() {
    const f = this.fixture();
    this.createController(f);
    expect(this.controller.study).toEqual(f.study);
    expect(this.controller.collectionEventType).toEqual(f.collectionEventType);
    expect(this.controller.annotationType).toEqual(f.annotationType);
  });

  it('state configuration is valid', function() {
    this.stateInit(this.fixture());
  });

  describe('shared behaviour', function () {
    var context = {};

    beforeEach(function () {
      const f = this.fixture();
      context.entity                       = this.CollectionEventType;
      context.updateAnnotationTypeFuncName = 'updateAnnotationType';
      context.parentObject                 = f.collectionEventType;
      context.annotationType               = f.annotationType;

      context.createController = () => { this.createController(f); };
    });

    sharedBehaviour(context);

  });

  describe('for removing an annotation type', function() {

    it('should send a request to the server', function() {
      const f = this.fixture();
      this.stateInit(f);
      this.createController(f);

      this.$httpBackend
        .whenDELETE(this.url('annottype',
                               f.study.id,
                               f.collectionEventType.id,
                               f.collectionEventType.version,
                               f.annotationType.id))
        .respond(this.reply(f.plainEventType));
      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(this.notificationsService, 'success').and.returnValue(null);

      this.controller.removeRequest();
      this.expectStudy(f.plainStudy);
      this.$httpBackend.flush();
      expect(this.notificationsService.success).toHaveBeenCalled();
      expect(this.$state.current.name).toBe('home.admin.studies.study.collection.ceventType');
    });

    it('should an error response from the server', function() {
      const f = this.fixture();
      this.stateInit(f);
      this.createController(f);

      this.$httpBackend
        .expectDELETE(this.url('annottype',
                               f.study.id,
                               f.collectionEventType.id,
                               f.collectionEventType.version,
                               f.annotationType.id))
        .respond(400, this.errorReply('simulated error'));
      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(this.notificationsService, 'success').and.returnValue(null);

      this.controller.removeRequest();
      this.expectStudy(f.plainStudy);
      this.$httpBackend.flush();
      expect(this.notificationsService.success).not.toHaveBeenCalled();
      expect(this.$state.current.name)
        .toBe('home.admin.studies.study.collection.ceventType.annotationTypeView');
  });

  });

});
