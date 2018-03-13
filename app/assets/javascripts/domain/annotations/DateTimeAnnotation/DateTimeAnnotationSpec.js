/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

/*
 * AnnotationSpec.js has test cases for all types of annotations.
 *
 * These test cases provide additional code coverage to the ones in AnnotationSpec.js.
 */
describe('DateTimeAnnotation', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(EntityTestSuiteMixin) {
      _.extend(this, EntityTestSuiteMixin);
      this.injectDependencies('DateTimeAnnotation',
                              'AnnotationType',
                              'AnnotationValueType',
                              'Factory');
    });
  });

  it('set value can be assigned a string', function() {
    var jsonAnnotationType = this.Factory.annotationType({ valueType: this.AnnotationValueType.DATE_TIME }),
        jsonAnnotation = this.Factory.annotation({}, jsonAnnotationType),
        annotationType = new this.AnnotationType(jsonAnnotationType),
        annotation = new this.DateTimeAnnotation(jsonAnnotation, annotationType);

    annotation.setValue('1990-01-01 12:00');
    expect(annotation.value).toBeDate();
  });

});
