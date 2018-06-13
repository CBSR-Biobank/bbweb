/**
 * Jasmine test suite

 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ProcessingTypeFixture from 'test/fixtures/ProcessingTypeFixture';
import { ServerReplyMixin } from 'test/mixins/ServerReplyMixin';
import ngModule from '../../index'
import sharedBehaviour from 'test/behaviours/annotationTypeViewComponentSharedBehaviour';

describe('Component: processingTypeAnnotationTypeView', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin, ServerReplyMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$httpBackend',
                              'notificationsService',
                              'Study',
                              'ProcessingType',
                              'CollectionEventType',
                              'AnnotationType',
                              'Factory');

      this.processingTypeFixture = new ProcessingTypeFixture(this.Factory,
                                                             this.Study,
                                                             this.CollectionEventType,
                                                             this.ProcessingType);

      this.createController = (study, processingType, plainProcessingType, annotationType) => {
        const url = this.url('studies/proctypes', study.slug, processingType.slug);
        this.$httpBackend.expectGET(url).respond(this.reply(plainProcessingType));

        ComponentTestSuiteMixin.createController.call(
          this,
          `<processing-type-annotation-type-view
              study="vm.study"
              processing-type="vm.processingType"
              annotation-type="vm.annotationType"
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

});
