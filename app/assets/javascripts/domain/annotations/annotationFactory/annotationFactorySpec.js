/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { AnnotationsEntityTestSuiteMixin } from 'test/mixins/AnnotationsEntityTestSuiteMixin';
import moment from 'moment';
import ngModule from '../../index'

/*
 * AnnotationSpec.js has test cases for all types of annotations.
 *
 * These test cases provide additional code coverage to the ones in AnnotationSpec.js.
 */
describe('annotationFactorySpec', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, AnnotationsEntityTestSuiteMixin);
      this.injectDependencies('annotationFactory',
                              'AnnotationType',
                              'AnnotationValueType',
                              'AnnotationMaxValueCount',
                              'Factory');
    });
  });

  it('can create an annotation of each value type', function() {
    Object.values(this.annotationTypesForAllValueTypes()).forEach((options) => {
      const entities = this.getAnnotationAndType(options);
      const annotation = this.annotationFactory.create(entities.jsonAnnotation, entities.annotationType);

      switch (entities.annotationType.valueType) {

      case this.AnnotationValueType.TEXT:
        expect(annotation.value).toBe(entities.jsonAnnotation.stringValue);
        break;

      case this.AnnotationValueType.NUMBER:
        expect(annotation.value).toBe(parseFloat(entities.jsonAnnotation.numberValue));
        break;

      case this.AnnotationValueType.DATE_TIME:
          expect(moment(annotation.value).utc().format()).toBe(entities.jsonAnnotation.stringValue);
        break;

      case this.AnnotationValueType.SELECT:
        if (entities.annotationType.isSingleSelect()) {
          expect(annotation.value).toBe(entities.jsonAnnotation.selectedValues[0]);
        } else if (entities.annotationType.isMultipleSelect()) {
          annotation.value.forEach((option) => {
            expect(entities.jsonAnnotation.selectedValues).toContain(option.name);
          });
        }
        break;

      default:
        // should never happen since this is checked for in the create method, but just in case
        fail('value type is invalid: ' + entities.annotationType.valueType);
      }
    });
  })

  it('cannot be created without an annotation type', function() {
    expect(() => {
      this.annotationFactory.create({});
    }).toThrowError(/annotation type is undefined/);
  });

  it('cannot be created with invalid value type', function() {
    const jsonAnnotationType = this.Factory.annotationType(),
          annotationType = new this.AnnotationType(jsonAnnotationType);

    annotationType.valueType = this.Factory.stringNext();

    expect(() => {
      this.annotationFactory.create({}, annotationType);
    }).toThrowError(/value type is invalid/);
  });

  it('attempt to create a select annotation with invalid max value count throws an error', function() {
    const jsonAnnotationType = this.Factory.annotationType({ valueType: this.AnnotationValueType.SELECT}),
          annotationType = new this.AnnotationType(jsonAnnotationType);
    annotationType.maxValueCount = -1;

    expect(() => {
      this.annotationFactory.create({}, annotationType);
    }).toThrowError(/invalid select annotation/);
  })

});
