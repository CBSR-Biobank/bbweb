/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 *
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'faker',
  'moment'
], function(angular, mocks, _, faker, moment) {
  'use strict';

  describe('Annotation', function() {

    var bbwebConfig,
        Study,
        AnnotationType,
        annotationFactory,
        Annotation,
        AnnotationValueType,
        jsonEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(entityTestSuite, testUtils, extendedDomainEntities) {
      _.extend(this, entityTestSuite);

      bbwebConfig                   = this.$injector.get('bbwebConfig');
      Study                         = this.$injector.get('Study');
      AnnotationType                = this.$injector.get('AnnotationType');
      annotationFactory             = this.$injector.get('annotationFactory');
      Annotation                    = this.$injector.get('Annotation');
      AnnotationValueType           = this.$injector.get('AnnotationValueType');
      jsonEntities                  = this.$injector.get('jsonEntities');

      testUtils.addCustomMatchers();
    }));

    describe('for Participants', function() {
      var context = {};

      beforeEach(function () {
        //var study = new Study(jsonEntities.study());

        context.annotationTypeType = AnnotationType;
        context.createAnnotationType = function (options) {
          options = options || {};
          return new AnnotationType(jsonEntities.annotationType(options));
        };
      });

      sharedBehaviour(context);
    });

    describe('for Collection Events', function() {
      var context = {};

      beforeEach(function () {
        //var study = new Study(jsonEntities.study());

        context.annotationTypeType = AnnotationType;
        context.createAnnotationType = function (options) {
          options = options || {};
          return new AnnotationType(jsonEntities.annotationType(options));
        };

      });

      sharedBehaviour(context);
    });

    describe('for Specimen Links', function() {
      var context = {};

      beforeEach(function () {
        //var study = new Study(jsonEntities.study());

        context.annotationTypeType = AnnotationType;
        context.createAnnotationType = function (options) {
          options = options || {};
          return new AnnotationType(jsonEntities.annotationType(options));
        };

      });

      sharedBehaviour(context);
    });

    function sharedBehaviour(context) {

      var annotationTypeType, createAnnotationType;

      describe('(shared)', function() {

        beforeEach(inject(function($state) {
          annotationTypeType = context.annotationTypeType;
          createAnnotationType = context.createAnnotationType;
        }));

        function createAnnotation(baseObj, annotationType) {
          return annotationFactory.create(baseObj, annotationType);
        }

        it('can create annotation with empty value', function() {
          _.each(getAnnotationTypeOptionsForAll(), function (annotationTypeOptions) {
            var annotationType,
                jsonAnnotation,
                annotation;

            annotationTypeOptions.required = true;

            annotationType = createAnnotationType(annotationTypeOptions);
            jsonAnnotation = jsonEntities.annotation(null, annotationType);
            annotation = annotationFactory.create(jsonAnnotation, annotationType);
            expect(annotation.getValue()).toBeFalsy();
          });
        });

        it('fails when creating without an annotation type', function() {
          expect(function () { return annotationFactory.create(undefined); })
            .toThrow(new Error('annotation type is undefined'));
        });

        it('fails when creating with required parameter omitted', function() {
          var annotationType;

          annotationType = createAnnotationType({ valueType: AnnotationValueType.TEXT() });

          expect(function () { return annotationFactory.create(undefined, annotationType); })
            .not.toThrow(new Error('required not assigned'));
        });

        it('creation fails if value type is invalid', function() {
          var annotationType = createAnnotationType({
            valueType: 'ABCDEF'
          });
          expect(function () { return createAnnotation(undefined, annotationType); })
            .toThrow(new Error('value type is invalid: ABCDEF'));
        });

        it('creation fails if max value count is invalid', function() {
          var annotationType = createAnnotationType({
            valueType: AnnotationValueType.SELECT()
          });
          annotationType.maxValueCount = 0;
          expect(function () { return createAnnotation(undefined, annotationType); })
            .toThrow(new Error('invalid select annotation: ' + annotationType.maxValueCount));

          annotationType.maxValueCount = 3;
          expect(function () { return createAnnotation(undefined, annotationType); })
            .toThrow(new Error('invalid select annotation: ' + annotationType.maxValueCount));
        });

        it('creation fails for SINGLE SELECT and multiple values are selected in server object',
           function() {
             var annotationType,
                 serverAnnotation,
                 value;

             annotationType = createAnnotationType({
               valueType:     AnnotationValueType.SELECT(),
               maxValueCount: 1,
               options:       [ 'option1', 'option2', 'option3' ],
               required:      true
             });

             serverAnnotation = jsonEntities.annotation(value, annotationType);
             serverAnnotation.selectedValues = annotationType.options;
             expect(function () { return createAnnotation(serverAnnotation, annotationType); })
               .toThrow(new Error('invalid selected values in object from server'));
           });

        it('fails when creating from a non object', function() {
          var annotationType = createAnnotationType({
            valueType:     AnnotationValueType.SELECT(),
            maxValueCount: 1,
            options:       [ 'option1', 'option2', 'option3' ],
            required:      true
          });
          expect(function () { annotationFactory.create(1, annotationType); })
            .toThrowErrorOfType('Error');
        });

        it('fails when creating from server response with bad selections', function() {
          var annotationType = createAnnotationType(
            {
              valueType:     AnnotationValueType.SELECT(),
              maxValueCount: 1,
              options:       [ 'option1', 'option2', 'option3' ],
              required:      true
            }),
              jsonAnnotation = {
                annotationTypeId: jsonEntities.stringNext(),
                selectedValues: { tmp: 1 }
          };
          expect(function () { annotationFactory.create(jsonAnnotation, annotationType); })
            .toThrowErrorOfType('Error');
        });

        it('has valid values when created from server response', function() {
          _.each(getAnnotationAndTypeForAllValueTypes(), function (entities) {
            entities.annotation.compareToJsonEntity(entities.serverAnnotation);
          });
        });

        it('calling getAnnotationTypeId gives a valid result', function() {
          _.each(getAnnotationAndTypeForAllValueTypes(), function (entities) {
            expect(entities.annotation.getAnnotationTypeId())
              .toBe(entities.annotationType.uniqueId);
          });
        });

        it('calling getValueType returns the annotation types value type', function() {
          _.each(getAnnotationAndTypeForAllValueTypes(), function (entities) {
            expect(entities.annotation.getValueType())
              .toBe(entities.annotationType.valueType);
          });
        });

        it('calling getLabel returns the annotation types name', function() {
          _.each(getAnnotationAndTypeForAllValueTypes(), function (entities) {
            expect(entities.annotation.getLabel())
              .toBe(entities.annotationType.name);
          });
        });

        it('calling isValid returns true if the annotation is not required', function() {
          _.each(getAnnotationAndTypeForAllValueTypes(), function (entities) {
            expect(entities.annotation.isValueValid()).toBe(true);
          });
        });

        it('calling isValueValid returns FALSE if the annotation is required and has no value', function() {
          _.each(getAnnotationTypeOptionsForAll(), function (annotationTypeOptions) {
            var annotationType,
                serverAnnotation,
                annotation;

            annotationTypeOptions.required = true;

            annotationType = createAnnotationType(annotationTypeOptions);
            serverAnnotation = jsonEntities.annotation(null, annotationType);
            annotation = annotationFactory.create(serverAnnotation, annotationType, true);
            expect(annotation.isValueValid()).toBe(false);
          });

        });

        it('calling isValueValid returns TRUE if the annotation is required and has a value', function() {
          _.each(getAnnotationTypeOptionsForAll(), function (annotationTypeOptions) {
            var annotationType,
                value,
                serverAnnotation,
                annotation;

            annotationTypeOptions.required = true;

            annotationType   = createAnnotationType(annotationTypeOptions);
            value            = jsonEntities.valueForAnnotation(annotationType);
            serverAnnotation = jsonEntities.annotation({ value: value }, annotationType);
            annotation       = annotationFactory.create(serverAnnotation, annotationType, true);
            expect(annotation.isValueValid()).toBe(true);
          });
        });

        it('getValue returns valid results for TEXT and DATE_TIME annotation types', function() {
          var annotationType,
              annotation,
              serverAnnotation,
              value,
              valueTypes = [ AnnotationValueType.TEXT(), AnnotationValueType.DATE_TIME() ],
              timeStr;

          _.each(valueTypes, function (valueType) {
            annotationType = createAnnotationType({ valueType: valueType });

            value = jsonEntities.valueForAnnotation(annotationType);
            serverAnnotation = jsonEntities.annotation({ value: value }, annotationType);
            annotation = createAnnotation(serverAnnotation, annotationType);

            if (valueType === AnnotationValueType.TEXT()) {
              expect(annotation.getValue()).toEqual(serverAnnotation.stringValue);
            } else {
              timeStr = moment(serverAnnotation.stringValue).local().format(bbwebConfig.dateTimeFormat);
              expect(annotation.getValue()).toEqual(timeStr);
            }

          });
        });

        it('getValue returns valid results for NUMBER annotation type', function() {
          var annotationType,
              annotation,
              serverAnnotation,
              value;

          annotationType = createAnnotationType({ valueType: AnnotationValueType.NUMBER() });

          value = jsonEntities.valueForAnnotation(annotationType);
          serverAnnotation = jsonEntities.annotation({ value: value }, annotationType);

          annotation = createAnnotation(serverAnnotation, annotationType);
          expect(annotation.getValue()).toEqual(parseFloat(serverAnnotation.numberValue));
        });

        it('getValue returns valid results for SINGLE SELECT', function() {
          var annotationType, annotation, serverAnnotation, value;

          annotationType = createAnnotationType({
            valueType:     AnnotationValueType.SELECT(),
            maxValueCount: 1,
            options:       [ 'option1', 'option2' ],
            required:      true
          });

          value = jsonEntities.valueForAnnotation(annotationType);
          serverAnnotation = jsonEntities.annotation({ value: value }, annotationType);

          annotation = createAnnotation(serverAnnotation, annotationType);
          expect(annotation.getValue()).toEqual(serverAnnotation.selectedValues[0].value);
        });

        it('getValue returns valid results for MULTIPLE SELECT', function() {
          var annotationType, annotation, serverAnnotation, value, multipleSelectValues;

          annotationType = createAnnotationType({
            valueType:     AnnotationValueType.SELECT(),
            maxValueCount: 2,
            options:       [ 'option1', 'option2', 'option3' ],
            required:      true
          });

          value = jsonEntities.valueForAnnotation(annotationType);
          serverAnnotation = jsonEntities.annotation({ value: value }, annotationType);

          annotation = createAnnotation(serverAnnotation, annotationType);
          multipleSelectValues = _.pluck(serverAnnotation.selectedValues, 'value');

          expect(annotation.getValue()).toEqual(multipleSelectValues.join(', '));
        });

        it('getServerAnnotation returns valid results for non select annotation types', function() {
          _.each(getAnnotationAndTypeForAllValueTypes(), function (entities) {
            expect(entities.annotation.getServerAnnotation()).toEqual(entities.serverAnnotation);
          });
        });

        it('getServerAnnotation returns valid results for non select annotation types', function() {
          _.each(getAnnotationAndTypeForAllValueTypes(), function (entities) {
            expect(entities.annotation.getServerAnnotation()).toEqual(entities.serverAnnotation);
          });
        });

        it('getServerAnnotation returns valid results for annotation with empty value', function() {
          _.each(getAnnotationTypeOptionsForAll(), function (annotationTypeOptions) {
            var annotationType,
                serverAnnotation,
                annotation;

            annotationTypeOptions.required = true;

            annotationType = createAnnotationType(annotationTypeOptions);
            serverAnnotation = jsonEntities.annotation({ value: '' }, annotationType);
            annotation = annotationFactory.create(serverAnnotation, annotationType, true);
            expect(annotation.getServerAnnotation()).toEqual(serverAnnotation);
          });
        });

        it('someSelected returns valid results for multiple select', function() {
          var annotationType, annotation;

          annotationType = createAnnotationType({
            valueType:     AnnotationValueType.SELECT(),
            maxValueCount: 2,
            options:       [ 'option1', 'option2' ],
            required:      true
          });

          annotation = createAnnotation(undefined, annotationType);

          expect(annotationType.options).not.toBeEmptyArray();
          expect(annotation.values).toBeArrayOfSize(annotationType.options.length);
          expect(annotation.someSelected()).toBe(false);

          annotation.values[0].checked = true;
          expect(annotation.someSelected()).toBe(true);
        });

        function getAnnotationAndType(annotTypeOptions) {
          var annotationType,
              value,
              jsonAnnotation,
              annotation;

          annotTypeOptions = annotTypeOptions || {};

          annotationType   = createAnnotationType(annotTypeOptions);
          value            = jsonEntities.valueForAnnotation(annotationType);
          jsonAnnotation   = jsonEntities.annotation({ value: value }, annotationType);
          annotation       = createAnnotation(jsonAnnotation, annotationType);

          return {
            annotationType:   annotationType,
            serverAnnotation: jsonAnnotation,
            annotation:       annotation
          };
        }

        /**
         * Creates annotation type options to create an annotation of each type of object.
         */
        function getAnnotationTypeOptionsForAll() {
          var result = _.map(AnnotationValueType.values(), function (valueType) {
            return { valueType: valueType };
          });
          result.push({
            valueType: AnnotationValueType.SELECT(),
            maxValueCount: 2,
            options: [ 'opt1', 'opt2', 'opt3' ]
          });
          return result;
        }

        /**
         * Creates a set of annotation type, server annotation and annotation object for each type
         * of annotation.
         */
        function getAnnotationAndTypeForAllValueTypes() {
          return _.map(getAnnotationTypeOptionsForAll(), function (annotationTypeOptions) {
            return getAnnotationAndType(annotationTypeOptions);
          });
        }

      });

    }

  });

});
