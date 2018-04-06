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
describe('TextAnnotation', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, EntityTestSuiteMixin);
      this.injectDependencies('TextAnnotation',
                              'AnnotationType',
                              'AnnotationValueType',
                              'Factory');
    });
  });

  describe('shared behaviour', function() {
    const context = {};

    beforeEach(function() {
      context.classType = this.TextAnnotation;
      context.annotationTypeJson =
        (options = {}) => this.Factory.annotationType(Object.assign(
          options, { valueType: this.AnnotationValueType.TEXT }))
      context.annotationJson =
        (options, annotationTypeJson) => this.Factory.annotation(options, annotationTypeJson)
      context.createAnnotation = this.TextAnnotation.create.bind(this.TextAnnotation);
    });

    annotationSharedBehaviour(context);

  })

});
