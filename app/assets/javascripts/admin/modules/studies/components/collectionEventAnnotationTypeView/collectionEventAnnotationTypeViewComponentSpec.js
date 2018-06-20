/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'
import sharedBehaviour from 'test/behaviours/annotationTypeViewComponentSharedBehaviour';

describe('Component: collectionEventAnnotationTypeView', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'notificationsService',
                              'Study',
                              'CollectionEventType',
                              'AnnotationType',
                              'Factory');

      this.init();
      this.url = (...paths) => {
        const args = [ 'studies/cetypes' ].concat(paths);
        return ComponentTestSuiteMixin.url(...args);
      };

      this.fixture = () => {
        const plainAnnotationType = this.Factory.annotationType();
        const plainEventType      = this.Factory.collectionEventType({
          annotationTypes: [ plainAnnotationType ]
        });
        const plainStudy          = this.Factory.study();
        const study               = this.Study.create();
        const collectionEventType = this.CollectionEventType.create(plainEventType);
        const annotationType      = this.AnnotationType.create(plainAnnotationType);

        return {
          plainStudy,
          study,
          plainEventType,
          collectionEventType,
          annotationType
        };
      };

      this.stateInit = (fixture) => {
        this.$httpBackend
          .whenGET(ComponentTestSuiteMixin.url('studies', fixture.plainStudy.slug))
          .respond(this.reply(fixture.plainStudy));

        this.$httpBackend
          .whenGET(this.url(fixture.plainStudy.slug, fixture.collectionEventType.slug))
          .respond(this.reply(fixture.plainEventType));

        this.gotoUrl(
          `/admin/studies/${fixture.plainStudy.slug}/collection/events/${fixture.plainEventType.slug}/annottypes/${fixture.annotationType.slug}`);
        this.$httpBackend.flush();
        expect(this.$state.current.name)
          .toBe('home.admin.studies.study.collection.ceventType.annotationTypeView');
      };

      this.createController = (fixture) => {
        this.$httpBackend
          .expectGET(this.url('studies/cetypes', fixture.study.slug))
          .respond(this.reply(this.Factory.pagedResult(fixture.plainEventTypes)));

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
      };
    });
  });

  it('should have  valid scope', function() {
    const f = this.fixture();
    this.createController(f);
    expect(this.controller.study).toBe(f.study);
    expect(this.controller.collectionEventType).toBe(f.collectionEventType);
    expect(this.controller.annotationType).toBe(f.annotationType);
  });

  fit('state configuration is valid', function() {
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

      context.createController = () => this.createController(f);
    });

    sharedBehaviour(context);

  });

  describe('for removing an annotation type', function() {

    fit('should send a request to the server', function() {
      const f = this.fixture();

      this.stateInit(f);
      this.createController(f);

      this.$httpBackend
        .expectDELETE(this.url('annottype',
                               f.study.id,
                               f.collectionEventType.id,
                               f.collectionEventType.version,
                               f.annotationType.id))
        .respond(this.reply(fixture.plainEventType));
      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(this.notificationsService, 'success').and.returnValue(null);

      this.controller.removeRequest();
      this.$httpBackend.flush();
      expect(this.notificationsService.success).toHaveBeenCalled();
      expect(this.$state.current.name).toBe('home.admin.studies.study.collection.ceventType');

   });

  });

});
