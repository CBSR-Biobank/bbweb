/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'

describe('annotationTypeSummaryComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function () {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$componentController',
                              'AnnotationType',
                              'Factory');

      this.annotationType = new this.AnnotationType(this.Factory.annotationType());

      this.createController = (annotationType = this.annotationType) => {
        ComponentTestSuiteMixin.createController.call(
          this,
          `<annotation-type-summary
              annotation-type="vm.annotationType">
           </annotation-type-summary>`,
          {
            annotationType
          },
          'ceventsList');
      };
    });
  });

  it('has valid scope', function() {
    this.createController();
    expect(this.scope.vm.annotationType).toBe(this.annotationType);
  });

});
