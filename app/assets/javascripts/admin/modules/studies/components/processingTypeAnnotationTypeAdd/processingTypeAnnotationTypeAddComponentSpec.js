/**
 * Jasmine test suite
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ProcessingTypeFixture from 'test/fixtures/ProcessingTypeFixture';
import ngModule from '../../index'
import sharedBehaviour from 'test/behaviours/annotationTypeAddComponentSharedBehaviour';

describe('Component: processingTypeAnnotationTypeAdd', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);
      this.injectDependencies('Study',
                              'ProcessingType',
                              'AnnotationType',
                              'CollectionEventType',
                              'Factory');

      this.processingTypeFixture = new ProcessingTypeFixture(this.Factory,
                                                             this.Study,
                                                             this.CollectionEventType,
                                                             this.ProcessingType);

      this.createController = (study, processingType) =>
        this.createControllerInternal(
          `<processing-type-annotation-type-add
              study="vm.study"
              processing-type="vm.processingType">
           </processing-type-annotation-type-add>`,
          {
            study,
            processingType
          },
          'processingTypeAnnotationTypeAdd');
    });
  });

  it('should have  valid scope', function() {
    const f = this.processingTypeFixture.fixture();
    const processingType = f.processingTypesFromCollected[0].processingType
    this.createController(f.study, processingType);
    expect(this.controller.processingType).toBe(processingType);
  });

  describe('for onSubmit and onCancel', function () {
    var context = {};

    beforeEach(function () {
      context.createController = () => {
        const f = this.processingTypeFixture.fixture();
        this.createController(f.study, f.processingTypesFromCollected[0].processingType);
        context.scope      = this.scope;
        context.controller = this.controller;
      };

      context.entity                    = this.ProcessingType;
      context.addAnnotationTypeFuncName = 'addAnnotationType';
      context.returnState               = '^';
    });

    sharedBehaviour(context);
  });

});
