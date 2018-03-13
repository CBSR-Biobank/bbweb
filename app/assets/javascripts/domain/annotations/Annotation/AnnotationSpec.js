/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 *
 * Jasmine test suite
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Annotation', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(EntityTestSuiteMixin, TestUtils) {
      _.extend(this, EntityTestSuiteMixin);
      this.injectDependencies('Study',
                              'AnnotationType',
                              'annotationFactory',
                              'Annotation',
                              'AnnotationValueType',
                              'AnnotationMaxValueCount',
                              'timeService',
                              'Factory');

      TestUtils.addCustomMatchers();

      this.createAnnotationType =
        (options = {}) => this.AnnotationType.create(this.Factory.annotationType(options));

      this.getAnnotationAndType = (annotTypeOptions = {}) => {
        const annotationType   = this.createAnnotationType(annotTypeOptions),
              value            = this.Factory.valueForAnnotation(annotationType),
              jsonAnnotation   = this.Factory.annotation({ value: value }, annotationType),
              annotation       = this.annotationFactory.create(jsonAnnotation, annotationType);

        return {
          annotationType:   annotationType,
          serverAnnotation: jsonAnnotation,
          annotation:       annotation
        };
      };

      /*
       * Creates annotation type options to create an annotation of each type of object.
       */
      this.getAnnotationTypeOptionsForAll = () => {
        const result = _.values(this.AnnotationValueType).map((valueType) => ({ valueType: valueType }));
        result.push({
          valueType: this.AnnotationValueType.SELECT,
          maxValueCount: this.AnnotationMaxValueCount.SELECT_MULTIPLE,
          options: [ 'opt1', 'opt2', 'opt3' ]
        });
        return result;
      };

      /*
       * Creates a set of annotation type, server annotation and annotation object for each type
       * of annotation.
       */
      this.getAnnotationAndTypeForAllValueTypes = () =>
        this.getAnnotationTypeOptionsForAll().map(
          (annotationTypeOptions) =>
            this.getAnnotationAndType(annotationTypeOptions)
        );
    });
  });

  it('constructor throws error for an invalid annotation value type', function() {
    const annotationType = this.createAnnotationType({ valueType: this.Factory.stringNext() });

    expect(() => {
      const annotation = new this.Annotation({}, annotationType);
      expect(annotation).not.toBeNull();
    }).toThrowError(/value type is invalid:/);
  });

  it('constructor throws error if annotation type is missing the required attribute', function() {
    const annotationType = _.omit(this.createAnnotationType(), 'required');

    expect(() => {
      const annotation = new this.Annotation({}, annotationType);
      expect(annotation).not.toBeNull();
    }).toThrowError(/required not defined/);
  });

  it('constructor throws error for select annotation type and is not multiple or single select', function() {
    const annotationType = this.createAnnotationType({ valueType: this.AnnotationValueType.SELECT });
    annotationType.maxValueCount = undefined;

    expect(() => {
      const annotation = new this.Annotation({}, annotationType);
      expect(annotation).not.toBeNull();
    }).toThrowError(/invalid value for max count/);
  });

  describe('when creating', function() {

    it('fails when creating from an object missing annotation type ID', function() {
      const annotationTypeJson = this.Factory.annotationType(),
            badAnnotationJson = _.omit(this.Factory.annotation({}, annotationTypeJson), 'annotationTypeId');

      expect(() => this.Annotation.create(badAnnotationJson))
        .toThrowError(/invalid object to create from/);
    });

    it('fails when creating from an object missing selected values', function() {
      const annotationTypeJson = this.Factory.annotationType(),
            badAnnotationJson = _.omit(this.Factory.annotation({}, annotationTypeJson), 'selectedValues');

      expect(() => this.Annotation.create(badAnnotationJson))
        .toThrowError(/invalid object to create from/);
    });

    it('can create annotation with empty value', function() {
      this.getAnnotationTypeOptionsForAll()
        .forEach((annotationTypeOptions) => {
          annotationTypeOptions.required = true;
          const annotationType = this.createAnnotationType(annotationTypeOptions),
                jsonAnnotation = this.Factory.annotation(undefined, annotationType),
                annotation = this.annotationFactory.create(jsonAnnotation, annotationType);
          expect(annotation.getDisplayValue()).toBeFalsy();
        });
    });

    it('fails when creating without an annotation type', function() {
      expect(() => this.annotationFactory.create({}))
        .toThrowError(/annotation type is undefined/);
    });

    it('fails when creating with required parameter omitted', function() {
      const annotationType = this.createAnnotationType({ valueType: this.AnnotationValueType.TEXT });

      expect(() => this.annotationFactory.create(undefined, annotationType))
        .not.toThrow(new Error('required not assigned'));
    });

    it('creation fails if value type is invalid', function() {
      const annotationType = this.createAnnotationType({ valueType: 'ABCDEF' });
      expect(() => this.annotationFactory.create(undefined, annotationType))
        .toThrowError('value type is invalid: ABCDEF');
    });

    it('creation fails if max value count is invalid', function() {
      const annotationType = this.createAnnotationType({ valueType: this.AnnotationValueType.SELECT });
      annotationType.maxValueCount = 0;
      expect(() => this.annotationFactory.create(undefined, annotationType))
        .toThrowError(/invalid select annotation/);

      annotationType.maxValueCount = 3;
      expect(() => this.annotationFactory.create(undefined, annotationType))
        .toThrowError(/invalid select annotation/);
    });

    it('creation fails for SINGLE SELECT and multiple values are selected in server object', function() {
      const annotationType = this.createAnnotationType({
        valueType:     this.AnnotationValueType.SELECT,
        maxValueCount: this.AnnotationMaxValueCount.SELECT_SINGLE,
        options:       [ 'option1', 'option2', 'option3' ],
        required:      true
      });

      const serverAnnotation = this.Factory.annotation(annotationType.options[2], annotationType);

      serverAnnotation.selectedValues = annotationType.options;

      expect(() => this.annotationFactory.create(serverAnnotation, annotationType))
        .toThrowError('invalid value for selected values');
    });

    it('fails when creating from a non object', function() {
      const annotationType = this.createAnnotationType({
              valueType:     this.AnnotationValueType.SELECT,
              maxValueCount: this.AnnotationMaxValueCount.SELECT_SINGLE,
              options:       [ 'option1', 'option2', 'option3' ],
              required:      true
            });
      expect(() => this.annotationFactory.create(1, annotationType))
        .toThrowError(/invalid annotation from server/);
    });

    it('fails when creating from server response with bad selections', function() {
      const annotationType = this.createAnnotationType({
              valueType:     this.AnnotationValueType.SELECT,
              maxValueCount: this.AnnotationMaxValueCount.SELECT_SINGLE,
              options:       [ 'option1', 'option2', 'option3' ],
              required:      true
            }),
            jsonAnnotation = {
              annotationTypeId: this.Factory.stringNext(),
              selectedValues: { tmp: 1 }
            };
      expect(() => this.annotationFactory.create(jsonAnnotation, annotationType))
        .toThrowError(/invalid annotation from server/);
    });

  });

  it('getAnnotationTypeId throws an error if annotation type was never assigned', function() {
    const annotation = new this.Annotation({});
    expect(() => annotation.getAnnotationTypeId())
      .toThrowError(/annotation type not assigned/);
  });

  it('calling getAnnotationTypeId gives a valid result', function() {
    this.getAnnotationAndTypeForAllValueTypes().forEach((entities) => {
      expect(entities.annotation.getAnnotationTypeId()).toBe(entities.annotationType.id);
    });
  });

  describe('calling getValueType', function() {

    it('calling getValueType returns the annotation types value type', function() {
      this.getAnnotationAndTypeForAllValueTypes().forEach((entities) => {
        expect(entities.annotation.getValueType())
          .toBe(entities.annotationType.valueType);
      });
    });

    it('throws an error if annotation type is not assigned', function() {
      const annotation = new this.Annotation({});
      expect(() => annotation.getValueType())
        .toThrowError(/annotation type not assigned/);
    });

  });

  describe('calling getLabel', function() {

    it('calling getLabel returns the annotation types name', function() {
      this.getAnnotationAndTypeForAllValueTypes().forEach((entities) => {
        expect(entities.annotation.getLabel())
          .toBe(entities.annotationType.name);
      });
    });

    it('throws an error if annotation type is not assigned', function() {
      const annotation = new this.Annotation({});
      expect(() => annotation.getLabel())
        .toThrowError(/annotation type not assigned/);
    });

  });

  describe('calling isValueValid', function() {

    it('returns true if the annotation is not required', function() {
      this.getAnnotationAndTypeForAllValueTypes().forEach((entities) => {
        expect(entities.annotation.isValueValid()).toBe(true);
      });
    });

    it('returns FALSE if the annotation is required and has no value', function() {
      this.getAnnotationTypeOptionsForAll()
        .forEach((annotationTypeOptions) => {
          annotationTypeOptions.required = true;
          const annotationType = this.createAnnotationType(annotationTypeOptions);
          const serverAnnotation = this.Factory.annotation(undefined, annotationType);
          const annotation = this.annotationFactory.create(serverAnnotation, annotationType, true);
          expect(annotation.isValueValid()).toBe(false);
      });

    });

    it('returns TRUE if the annotation is required and has a value', function() {
      this.getAnnotationTypeOptionsForAll()
        .forEach((annotationTypeOptions) => {
          annotationTypeOptions.required = true;
          const annotationType   = this.createAnnotationType(annotationTypeOptions);
          const value            = this.Factory.valueForAnnotation(annotationType);
          const serverAnnotation = this.Factory.annotation({ value: value }, annotationType);
          const annotation       = this.annotationFactory.create(serverAnnotation, annotationType, true);
          expect(annotation.isValueValid()).toBe(true);
        });
    });

  });

  it('getValue returns valid results for TEXT and DATE_TIME annotation types', function() {
    const valueTypes = [ this.AnnotationValueType.TEXT, this.AnnotationValueType.DATE_TIME ];

    valueTypes.forEach((valueType) => {
      const annotationType = this.createAnnotationType({ valueType: valueType });
      const value = this.Factory.valueForAnnotation(annotationType);
      const serverAnnotation = this.Factory.annotation({ value: value }, annotationType);
      const annotation = this.annotationFactory.create(serverAnnotation, annotationType);

      if (valueType === this.AnnotationValueType.TEXT) {
        expect(annotation.getValue()).toEqual(serverAnnotation.stringValue);
      } else {
        const timeStr = this.timeService.dateToDisplayString(serverAnnotation.stringValue);
        expect(annotation.getValue()).toEqual(timeStr);
      }

    });
  });

  it('getValue returns valid results for NUMBER annotation type', function() {
    const annotationType = this.createAnnotationType({ valueType: this.AnnotationValueType.NUMBER });
    const value = this.Factory.valueForAnnotation(annotationType);
    const serverAnnotation = this.Factory.annotation({ value: value }, annotationType);
    const annotation = this.annotationFactory.create(serverAnnotation, annotationType);

    expect(annotation.getValue()).toEqual(parseFloat(serverAnnotation.numberValue));
  });

  it('getValue returns valid results for SINGLE SELECT', function() {
    const annotationType = this.createAnnotationType({
      valueType:     this.AnnotationValueType.SELECT,
      maxValueCount: this.AnnotationMaxValueCount.SELECT_SINGLE,
      options:       [ 'option1', 'option2' ],
      required:      true
    });

    const value = this.Factory.valueForAnnotation(annotationType);
    const serverAnnotation = this.Factory.annotation({ value: value }, annotationType);
    const annotation = this.annotationFactory.create(serverAnnotation, annotationType);

    expect(annotation.getValue()).toEqual(serverAnnotation.selectedValues[0]);
  });

  it('getDisplayValue returns valid results for MULTIPLE SELECT', function() {
    const annotationType = this.createAnnotationType({
      valueType:     this.AnnotationValueType.SELECT,
      maxValueCount: this.AnnotationMaxValueCount.SELECT_MULTIPLE,
      options:       [ 'option1', 'option2', 'option3' ],
      required:      true
    });

    const value = this.Factory.valueForAnnotation(annotationType);
    const serverAnnotation = this.Factory.annotation({ value: value }, annotationType);
    const annotation = this.annotationFactory.create(serverAnnotation, annotationType);

    expect(annotation.getDisplayValue()).toEqual(serverAnnotation.selectedValues.join(', '));
  });

  it('getServerAnnotation returns valid results for non select annotation types', function() {
    this.getAnnotationAndTypeForAllValueTypes().forEach((entities) => {
      const serverAnnot = entities.annotation.getServerAnnotation();
      _.keys(serverAnnot).forEach((key) => {
        expect(serverAnnot[key]).toEqual(entities.serverAnnotation[key]);
      });
    });
  });

  it('getServerAnnotation returns valid results for annotation with empty value', function() {
    this.getAnnotationTypeOptionsForAll()
      .forEach((annotationTypeOptions) => {
        annotationTypeOptions.required = true;
        const annotationType = this.createAnnotationType(annotationTypeOptions);
        const serverAnnotation = this.Factory.annotation({ value: '' }, annotationType);
        const annotation = this.annotationFactory.create(serverAnnotation, annotationType, true);

        const serverAnnot = annotation.getServerAnnotation();
        _.keys(serverAnnot).forEach((key) => {
          expect(serverAnnot[key]).toEqual(serverAnnotation[key]);
        });
      });
  });

  it('someSelected returns valid results for multiple select', function() {
    const annotationType = this.createAnnotationType({
      valueType:     this.AnnotationValueType.SELECT,
      maxValueCount: this.AnnotationMaxValueCount.SELECT_MULTIPLE,
      options:       [ 'option1', 'option2' ],
      required:      true
    });

    const annotation = this.annotationFactory.create(undefined, annotationType);

    expect(annotationType.options).not.toBeEmptyArray();
    expect(annotation.values).toBeArrayOfSize(annotationType.options.length);
    expect(annotation.someSelected()).toBe(false);

    annotation.values[0].checked = true;
    expect(annotation.someSelected()).toBe(true);
  });

  it('calling setValue assigns the value', function() {
    const annotation = new this.Annotation(),
          newValue = this.Factory.stringNext();
    annotation.setValue(newValue);
    expect(annotation.value).toBe(newValue);
  });

});
