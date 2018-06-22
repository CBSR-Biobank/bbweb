/**
 * Jasmine test suite

 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ProcessingTypeFixture from 'test/fixtures/ProcessingTypeFixture';
import ngModule from '../../../../../app'
import sharedBehaviour from 'test/behaviours/annotationTypeViewComponentSharedBehaviour';

describe('Component: processingTypeAnnotationTypeView', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$httpBackend',
                              '$state',
                              'domainNotificationService',
                              'notificationsService',
                              'modalService',
                              'Study',
                              'ProcessingType',
                              'CollectionEventType',
                              'AnnotationType',
                              'Factory');

      this.processingTypeFixture = new ProcessingTypeFixture(this.Factory,
                                                             this.Study,
                                                             this.CollectionEventType,
                                                             this.ProcessingType);

      this.init();
      this.url = (...paths) => {
        const args = [ 'studies/proctypes' ].concat(paths);
        return ComponentTestSuiteMixin.url(...args);
      };

      this.stateInit = (plainStudy, plainProcessingType, annotationType) => {
        this.$httpBackend
          .whenGET(ComponentTestSuiteMixin.url('studies', plainStudy.slug))
          .respond(this.reply(plainStudy));

        this.$httpBackend
          .whenGET(this.url(plainStudy.slug, plainProcessingType.slug))
          .respond(this.reply(plainProcessingType));

        this.gotoUrl(
          `/admin/studies/${plainStudy.slug}/processing/step/${plainProcessingType.slug}/annottypes/${annotationType.slug}`);
        this.$httpBackend.flush();
        expect(this.$state.current.name)
          .toBe('home.admin.studies.study.processing.viewType.annotationTypeView');
      };

      this.createController = (study, processingType, plainProcessingType, annotationType) => {
        this.$httpBackend
          .whenGET(this.url(study.slug, processingType.slug))
          .respond(this.reply(plainProcessingType));

        this.createControllerInternal(
          `<processing-type-annotation-type-view
              study="vm.study"
              processing-type="vm.processingType"
              annotation-type="vm.annotationType">
           </processing-type-annotation-type-view>`,
          {
            study,
            processingType,
            annotationType
          },
          'processingTypeAnnotationTypeView');
        this.$httpBackend.flush();
      };
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation()
    this.$httpBackend.verifyNoOutstandingRequest()
  });

  it('should have  valid scope', function() {
    const f = this.processingTypeFixture.fixture();
    const plainProcessingType = f.processingTypesFromCollected[0].plainProcessingType;
    const processingType = f.processingTypesFromCollected[0].processingType;
    const annotationType = processingType.annotationTypes[0];

    expect(processingType.annotationTypes).toBeNonEmptyArray();

    this.createController(f.study, processingType, plainProcessingType, annotationType);
    expect(this.controller.study).toBe(f.study);
    expect(this.controller.processingType).toEqual(processingType);
    expect(this.controller.annotationType).toEqual(annotationType);
  });

  describe('shared behaviour', function () {
    const context = {};

    beforeEach(function () {
      const f = this.processingTypeFixture.fixture();
      const plainProcessingType = f.processingTypesFromCollected[0].plainProcessingType;
      const processingType = f.processingTypesFromCollected[0].processingType;
      const annotationType = processingType.annotationTypes[0];

      context.entity                       = this.ProcessingType;
      context.updateAnnotationTypeFuncName = 'updateAnnotationType';
      context.parentObject                 = processingType;
      context.annotationType               = annotationType;
      context.createController             = () => {
        this.createController(f.study, processingType, plainProcessingType, annotationType);
      };
    });

    sharedBehaviour(context);

  });

  it('state configuration is valid', function() {
    const f = this.processingTypeFixture.fixture();
    const plainProcessingType = f.processingTypesFromCollected[0].plainProcessingType;
    const processingType = f.processingTypesFromCollected[0].processingType;
    const annotationType = processingType.annotationTypes[0];

    this.stateInit(f.plainStudy, plainProcessingType, annotationType);
  });

  describe('for removing an annotation type', function() {

    it('should send a request to the server', function() {
      const f = this.processingTypeFixture.fixture();
      const plainProcessingType = f.processingTypesFromCollected[0].plainProcessingType;
      const processingType = f.processingTypesFromCollected[0].processingType;
      const annotationType = processingType.annotationTypes[0];

      this.stateInit(f.plainStudy, plainProcessingType, annotationType);
      this.createController(f.study, processingType, plainProcessingType, annotationType);

      this.$httpBackend
        .expectDELETE(this.url('annottype',
                               f.study.id,
                               processingType.id,
                               processingType.version,
                               annotationType.id))
        .respond(this.reply(plainProcessingType));
      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(this.notificationsService, 'success').and.returnValue(null);

      this.controller.removeRequest();
      this.$httpBackend.flush();
      expect(this.notificationsService.success).toHaveBeenCalled();
      expect(this.$state.current.name).toBe('home.admin.studies.study.processing.viewType');
   });

    it('should handle an error response from the server', function() {
      const f = this.processingTypeFixture.fixture();
      const plainProcessingType = f.processingTypesFromCollected[0].plainProcessingType;
      const processingType = f.processingTypesFromCollected[0].processingType;
      const annotationType = processingType.annotationTypes[0];

      this.stateInit(f.plainStudy, plainProcessingType, annotationType);
      this.createController(f.study, processingType, plainProcessingType, annotationType);

      this.$httpBackend
        .expectDELETE(this.url('annottype',
                               f.study.id,
                               processingType.id,
                               processingType.version,
                               annotationType.id))
        .respond(400, this.errorReply('simulated error'));
      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(this.notificationsService, 'success').and.returnValue(null);

      this.controller.removeRequest();
      this.$httpBackend.flush();
      expect(this.notificationsService.success).not.toHaveBeenCalled();
      expect(this.$state.current.name)
        .toBe('home.admin.studies.study.processing.viewType.annotationTypeView');
 });

  });

});
