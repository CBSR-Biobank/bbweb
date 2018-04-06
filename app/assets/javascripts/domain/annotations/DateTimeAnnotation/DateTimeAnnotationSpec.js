/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { EntityTestSuiteMixin } from 'test/mixins/EntityTestSuiteMixin';
import annotationSharedBehaviour from 'test/behaviours/annotationSharedBehaviour';
import ngModule from '../../index'

/*
 * AnnotationSpec.js has test cases for all types of annotations.
 *
 * These test cases provide additional code coverage to the ones in AnnotationSpec.js.
 */
describe('DateTimeAnnotation', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, EntityTestSuiteMixin);
      this.injectDependencies('DateTimeAnnotation',
                              'AnnotationType',
                              'AnnotationValueType',
                              'timeService',
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

  describe('shared behaviour', function() {
    const context = {};

    beforeEach(function() {
      context.classType = this.DateTimeAnnotation;
      context.annotationTypeJson =
        (options = {}) => this.Factory.annotationType(Object.assign(
          options, { valueType: this.AnnotationValueType.DATE_TIME }))
      context.annotationJson =
        (options, annotationTypeJson) => this.Factory.annotation(options, annotationTypeJson)
      context.createAnnotation = this.DateTimeAnnotation.create.bind(this.DateTimeAnnotation);
    });

    annotationSharedBehaviour(context);

  })

});
