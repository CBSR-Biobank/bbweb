/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  /*
   * AnnotationSpec.js has test cases for all types of annotations.
   *
   * These test cases provide additional code coverage to the ones in AnnotationSpec.js.
   */
  describe('MultipleSelectAnnotation', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(EntityTestSuiteMixin) {
      _.extend(this, EntityTestSuiteMixin.prototype);
      this.injectDependencies('annotationFactory',
                              'AnnotationType',
                              'AnnotationValueType',
                              'AnnotationMaxValueCount',
                              'factory');
    }));

    it('cannot be created with invalid selected values', function() {
      var self = this,
          jsonAnnotationType = self.factory.annotationType({
            valueType:     self.AnnotationValueType.SELECT,
            maxValueCount: self.AnnotationMaxValueCount.SELECT_MULTIPLE,
            options:       [ 'option1', 'option2' ],
            required:      true
          }),
          annotationType = new self.AnnotationType(jsonAnnotationType),
          jsonAnnotation = self.factory.annotation({ selectedValues: [ this.factory.stringNext() ] },
                                                   jsonAnnotationType),
          annotation;

      expect(function () {
        annotation = self.annotationFactory.create(jsonAnnotation, annotationType);
      }).toThrowError('invalid selected values in object from server');
    });

  });

});
