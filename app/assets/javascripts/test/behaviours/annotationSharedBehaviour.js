/**
 * Jasmine shared behaviour.
 *
 * @namespace test.behaviours.annotationSharedBehaviour
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

/**
 * Context object to share information between the test suite and this shared behaviour.
 *
 * @typedef test.behaviours.annotationSharedBehaviour.Context
 * @type object
 *
 * @param {class} classType - the class of annotation being tested.
 *
 * @param {function} annotationTypeJson - a function that returns a plain object containing {@link
 * domain.annotations.AnnotationType AnnotationType} information.
 *
 * @param {function} annotationJson - a function that returns a plain object containing {@link
 * domain.annotations.Annotation Annotation} information.
 *
 * @param {function} createAannotation - a function that returns an object derived from {@link
 * domain.annotations.Annotation Annotation}.
 *
 * @see domain.annotations.DateTimeAnnotation
 * @see domain.annotations.MultipleSelectAnnotation
 * @see domain.annotations.NumberAnnotation
 * @see domain.annotations.SingleSelectAnnotation
 * @see domain.annotations.TextAnnotation
 */

/**
 * Common behaviour for test suites that test {@link domain.annotations.Annotation Annotations}.
 *
 * @function annotationSharedBehaviour
 * @memberOf test.behaviours.annotationSharedBehaviour
 *
 * @param {test.behaviours.annotationSharedBehaviour.Context} context
 */
export default function annotationSharedBehaviour(context) {

  describe('(shared)', function() {

    beforeEach(angular.mock.inject(function () {
      this.injectDependencies('AnnotationMaxValueCount');

      this.createEntities = (context, options = { annotationTypeOpts: {} }) => {
        const annotationTypeJson    = context.annotationTypeJson(options.annotationTypeOpts);
        const annotationType        = this.AnnotationType.create(annotationTypeJson);
        const value                 = this.Factory.valueForAnnotation(annotationTypeJson);
        const annotationJson        = context.annotationJson({ value }, annotationTypeJson);
        const annotation            = context.createAnnotation(annotationJson, annotationType, true);

        return {
          annotationTypeJson,
          annotationType,
          value,
          annotationJson,
          annotation
        };
      };
    }));

    it('can be constructed with an annotation type', function() {
      const entities = this.createEntities(context);
      const annotation = new context.classType(undefined, entities.annotationType);
      expect(annotation.annotationTypeId).toEqual(entities.annotationType.id);
    });

    it('constructor throws error for an invalid annotation value type', function() {
      const entities = this.createEntities(context);
      entities.annotationType.valueType = this.Factory.stringNext();

      expect(() => {
        const annotation = new context.classType({}, entities.annotationType);
        expect(annotation).not.toBeNull();
      }).toThrowError(/value type is invalid:/);
    });

    it('constructor throws error if annotation type is missing the required attribute', function() {
      const entities = this.createEntities(context);
      entities.annotationType.required = undefined;

      expect(() => {
        const annotation = new context.classType({}, entities.annotationType);
        expect(annotation).not.toBeNull();
      }).toThrowError(/required not defined/);
    });

    it('constructor throws error for select annotation type and is not multiple or single select',
       function() {
         const entities = this.createEntities(context);

         if (entities.annotationTypeJson.valueType !== this.AnnotationValueType.SELECT) {
           return;
         }

         entities.annotationType.maxValueCount = undefined;

         expect(() => {
           const annotation = new context.classType({}, entities.annotationType);
           expect(annotation).not.toBeNull();
         }).toThrowError(/invalid value for max count/);
       });

    it('can create with an annotation type', function() {
      const entities = this.createEntities(context);
      const annotation = context.classType.create({}, entities.annotationType);
      expect(annotation.annotationTypeId).toEqual(entities.annotationType.id);
    });

    it('can create from an object missing annotation type ID', function() {
      const entities = this.createEntities(context);
      const badAnnotationJson = _.omit(entities.annotationJson, 'annotationTypeId');

      const result = context.createAnnotation(badAnnotationJson, entities.annotationType);
      expect(result).toEqual(jasmine.any(context.classType));
    });

    it('fails when creating without an annotation type', function() {
      expect(() => context.createAnnotation({}))
        .toThrowError(/annotation type is undefined/);
    });

    it('fails when creating from a non object', function() {
      const annotationTypeJson = context.annotationTypeJson(),
            annotationType = this.AnnotationType.create(annotationTypeJson);
      expect(() => context.createAnnotation(1, annotationType))
        .toThrowError(/is not an object/);
    });

    it('calling getAnnotationTypeId gives a valid result', function() {
      const entities = this.createEntities(context);
      expect(entities.annotation.getAnnotationTypeId()).toBe(entities.annotationType.id);
    });

    it('getAnnotationTypeId throws an error if annotation type was never assigned', function() {
      const annotation = new context.classType({});
      expect(() => annotation.getAnnotationTypeId())
        .toThrowError(/annotation type not assigned/);
    });

    it('getServerAnnotation returns valid results for annotation with empty value', function() {
      const entities = this.createEntities(context);
      const actual = entities.annotation.getServerAnnotation();
      Object.keys(actual).forEach((key) => {
        expect(actual[key]).toEqual(entities.annotationJson[key]);
      });
    });

    it('getServerAnnotation returns valid results for non select annotation types', function() {
      const entities = this.createEntities(context);

      if (entities.annotationTypeJson.valueType === this.AnnotationValueType.SELECT) {
        return;
      }

      const serverAnnotation = entities.annotation.getServerAnnotation();
      Object.keys(serverAnnotation).forEach((key) => {
        expect(serverAnnotation[key]).toEqual(entities.annotationJson[key]);
      });
    });

    it('getValue returns valid results for TEXT and DATE_TIME annotation types', function() {
      const entities = this.createEntities(context);

      if ((entities.annotationTypeJson.valueType !== this.AnnotationValueType.TEXT) &&
          (entities.annotationTypeJson.valueType !== this.AnnotationValueType.DATE_TIME)) {
        return;
      }

      if (entities.annotationTypeJson.valueType === this.AnnotationValueType.TEXT) {
        expect(entities.annotation.getValue()).toEqual(entities.annotationJson.stringValue);
      } else {
        const timeStr = this.timeService.dateToDisplayString(entities.annotationJson.stringValue);
        expect(entities.annotation.getValue()).toEqual(timeStr);
      }

    });

    describe('calling getValueType', function() {

      it('calling getValueType returns the annotation types value type', function() {
        const entities = this.createEntities(context);
        expect(entities.annotation.getValueType())
          .toBe(entities.annotationType.valueType);
      });

      it('throws an error if annotation type is not assigned', function() {
        const annotation = new context.classType({});
        expect(() => annotation.getValueType())
          .toThrowError(/annotation type not assigned/);
      });

    });

    describe('calling getLabel', function() {

      it('calling getLabel returns the annotation types name', function() {
        const entities = this.createEntities(context);
        expect(entities.annotation.getLabel())
          .toBe(entities.annotationType.name);
      });

      it('throws an error if annotation type is not assigned', function() {
        const annotation = new context.classType({});
        expect(() => annotation.getLabel())
          .toThrowError(/annotation type not assigned/);
      });

    });

    describe('calling isValueValid', function() {

      it('returns true if the annotation is not required', function() {
        const entities = this.createEntities(context, { annotationTypeOpts: { required: true } });
        expect(entities.annotation.isValueValid()).toBe(true);
      });

      it('returns FALSE if the annotation is required and has no value', function() {
        const annotationTypeJson    = context.annotationTypeJson({ required: true });
        const annotationType        = this.AnnotationType.create(annotationTypeJson);
        const annotationJson        = context.annotationJson({}, annotationTypeJson);
        const annotation            = context.createAnnotation(annotationJson, annotationType, true);
        expect(annotation.isValueValid()).toBe(false);
      });

      it('returns TRUE if the annotation is required and has a value', function() {
        const entities = this.createEntities(context, { annotationTypeOpts: { required: true } });
        expect(entities.annotation.isValueValid()).toBe(true);
      });

    });

  });

}
