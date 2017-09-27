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
describe('MultipleSelectAnnotation', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(EntityTestSuite) {
      _.extend(this, EntityTestSuite.prototype);
      this.injectDependencies('MultipleSelectAnnotation',
                              'AnnotationType',
                              'AnnotationValueType',
                              'AnnotationMaxValueCount',
                              'factory');
    });
  });

  it('set value can be assigned a string', function() {
    var self = this,
        jsonAnnotationType = self.factory.annotationType({
          valueType:     self.AnnotationValueType.SELECT,
          maxValueCount: self.AnnotationMaxValueCount.SELECT_MULTIPLE,
          options:       [ 'option1', 'option2' ],
          required:      true
        }),
        jsonAnnotation = self.factory.annotation({}, jsonAnnotationType),
        annotationType = new self.AnnotationType(jsonAnnotationType),
        annotation = new self.MultipleSelectAnnotation(jsonAnnotation, annotationType);

    expect(function () {
      annotation.setValue('option1');
    }).toThrowError('value is not an array');
  });

});
