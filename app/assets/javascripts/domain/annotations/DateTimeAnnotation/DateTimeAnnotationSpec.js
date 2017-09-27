/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

/*
 * AnnotationSpec.js has test cases for all types of annotations.
 *
 * These test cases provide additional code coverage to the ones in AnnotationSpec.js.
 */
describe('DateTimeAnnotation', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(EntityTestSuite) {
      _.extend(this, EntityTestSuite.prototype);
      this.injectDependencies('DateTimeAnnotation',
                              'AnnotationType',
                              'AnnotationValueType',
                              'factory');
    });
  });

  it('set value can be assigned a string', function() {
    var jsonAnnotationType = this.factory.annotationType({ valueType: this.AnnotationValueType.DATE_TIME }),
        jsonAnnotation = this.factory.annotation({}, jsonAnnotationType),
        annotationType = new this.AnnotationType(jsonAnnotationType),
        annotation = new this.DateTimeAnnotation(jsonAnnotation, annotationType);

    annotation.setValue('1990-01-01 12:00');
    expect(annotation.value).toBeDate();
  });

});
