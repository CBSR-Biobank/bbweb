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
  'moment',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, faker, moment, testUtils) {
  'use strict';

  describe('Annotation', function() {

    var Study,
        ParticipantAnnotationType,
        CollectionEventAnnotationType,
        SpecimenLinkAnnotationType,
        annotationFactory,
        Annotation,
        AnnotationValueType,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_Study_,
                               _ParticipantAnnotationType_,
                               _CollectionEventAnnotationType_,
                               _SpecimenLinkAnnotationType_,
                               _annotationFactory_,
                               _Annotation_,
                               _AnnotationValueType_,
                               fakeDomainEntities,
                               extendedDomainEntities) {
      Study                         = _Study_;
      ParticipantAnnotationType     = _ParticipantAnnotationType_;
      CollectionEventAnnotationType = _CollectionEventAnnotationType_;
      SpecimenLinkAnnotationType    = _SpecimenLinkAnnotationType_;
      annotationFactory             = _annotationFactory_;
      Annotation                    = _Annotation_;
      AnnotationValueType           = _AnnotationValueType_;
      fakeEntities                  = fakeDomainEntities;

      testUtils.addCustomMatchers();
    }));

    describe('for Participants', function() {
      var context = {};

      beforeEach(function () {
        var study = new Study(fakeEntities.study());

        context.annotationTypeType = ParticipantAnnotationType;
        context.createAnnotationType = function (options) {
          options = options || {};
          return new ParticipantAnnotationType(
            fakeEntities.studyAnnotationType(study, options));
        };
      });

      sharedBehaviour(context);
    });

    describe('for Collection Events', function() {
      var context = {};

      beforeEach(function () {
        var study = new Study(fakeEntities.study());

        context.annotationTypeType = CollectionEventAnnotationType;
        context.createAnnotationType = function (options) {
          options = options || {};
          return new CollectionEventAnnotationType(
            fakeEntities.studyAnnotationType(study, options));
        };

      });

      sharedBehaviour(context);
    });

    describe('for Specimen Links', function() {
      var context = {};

      beforeEach(function () {
        var study = new Study(fakeEntities.study());

        context.annotationTypeType = SpecimenLinkAnnotationType;
        context.createAnnotationType = function (options) {
          options = options || {};
          return new SpecimenLinkAnnotationType(
            fakeEntities.studyAnnotationType(study, options));
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

        function getRequired() {
          if (annotationTypeType === ParticipantAnnotationType) {
            return undefined;
          }
          return true;
        }

        function createAnnotation(baseObj, annotationType) {
          return annotationFactory.create(baseObj, annotationType, getRequired());
        }

        it('can create annotation with empty value', function() {
          _.each(getAnnotationTypeOptionsForAll(), function (annotationTypeOptions) {
            var annotationType,
                serverAnnotation,
                annotation;

            if (annotationTypeType === ParticipantAnnotationType) {
              annotationTypeOptions.required = true;
            }

            annotationType = createAnnotationType(annotationTypeOptions);
            serverAnnotation = fakeEntities.annotation(null, annotationType);
            annotation = annotationFactory.create(serverAnnotation, annotationType, true);
            expect(annotation.getValue()).toBeFalsy();
          });
        });

        it('fails when creating without an annotation type', function() {
          expect(function () { return annotationFactory.create(undefined); })
            .toThrow(new Error('annotation type is undefined'));
        });

        it('fails when creating with required parameter omitted', function() {
          var annotationType;

          annotationType = createAnnotationType({
            valueType: AnnotationValueType.TEXT()
          });

          if (annotationTypeType === ParticipantAnnotationType) {
            expect(function () { return annotationFactory.create(undefined, annotationType); })
              .not.toThrow(new Error('required not assigned'));
          } else {
            expect(function () { return annotationFactory.create(undefined, annotationType); })
              .toThrow(new Error('required not assigned'));
          }
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

             serverAnnotation = fakeEntities.annotation(value, annotationType);
             serverAnnotation.selectedValues = _.map(annotationType.options, function(opt){
               return { annotationTypeId: annotationType.id, value: opt };
             });
             expect(function () { return createAnnotation(serverAnnotation, annotationType); })
               .toThrow(new Error('invalid value for selected values'));
           });

        it('fails when creating from a non object', function() {
          expect(function () { annotationFactory.create(1); })
            .toThrow(new Error('invalid object from server: has the correct keys'));
        });

        it('fails when creating from server response with bad selections', function() {
          var serverObj = {
            annotationTypeId: fakeEntities.stringNext(),
            selectedValues: { tmp: 1 }
          };
          expect(function () { annotationFactory.create(serverObj); })
            .toThrow(new Error('invalid selected values in object from server'));
        });

        it('has valid values when created from server response', function() {
          _.each(getAnnotationAndTypeForAllValueTypes(), function (entities) {
            entities.annotation.compareToServerEntity(entities.serverAnnotation);
          });
        });

        it('calling getAnnotationTypeId gives a valid result', function() {
          _.each(getAnnotationAndTypeForAllValueTypes(), function (entities) {
            expect(entities.annotation.getAnnotationTypeId())
              .toBe(entities.annotationType.id);
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
            expect(entities.annotation.isValid()).toBe(true);
          });
        });

        it('calling isValid returns FALSE if the annotation is required and has no value', function() {
          _.each(getAnnotationTypeOptionsForAll(), function (annotationTypeOptions) {
            var annotationType,
                serverAnnotation,
                annotation;

            if (annotationTypeType === ParticipantAnnotationType) {
              annotationTypeOptions.required = true;
            }

            annotationType = createAnnotationType(annotationTypeOptions);
            serverAnnotation = fakeEntities.annotation(null, annotationType);
            annotation = annotationFactory.create(serverAnnotation, annotationType, true);
            expect(annotation.isValid()).toBe(false);
          });

        });

        it('calling isValid returns TRUE if the annotation is required and has a value', function() {
          _.each(getAnnotationTypeOptionsForAll(), function (annotationTypeOptions) {
            var annotationType,
                value,
                serverAnnotation,
                annotation;

            if (annotationTypeType === ParticipantAnnotationType) {
              annotationTypeOptions.required = true;
            }

            annotationType   = createAnnotationType(annotationTypeOptions);
            value            = fakeEntities.valueForAnnotation(annotationType);
            serverAnnotation = fakeEntities.annotation(value, annotationType);
            annotation       = annotationFactory.create(serverAnnotation, annotationType, true);
            expect(annotation.isValid()).toBe(true);
          });
        });

        it('getValue returns valid results for TEXT and DATE_TIME annotation types', function() {
          var annotationType,
              annotation,
              serverAnnotation,
              value,
              valueTypes = [
                AnnotationValueType.TEXT(),
                AnnotationValueType.DATE_TIME()
              ];

          _.each(valueTypes, function (valueType) {
            annotationType = createAnnotationType({ valueType: valueType });

            value = fakeEntities.valueForAnnotation(annotationType);
            serverAnnotation = fakeEntities.annotation(value, annotationType);

            annotation = createAnnotation(serverAnnotation, annotationType);
            expect(annotation.getValue()).toEqual(serverAnnotation.stringValue);
          });
        });

        it('getValue returns valid results for NUMBER annotation type', function() {
          var annotationType,
              annotation,
              serverAnnotation,
              value;

          annotationType = createAnnotationType({ valueType: AnnotationValueType.NUMBER() });

          value = fakeEntities.valueForAnnotation(annotationType);
          serverAnnotation = fakeEntities.annotation(value, annotationType);

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

          value = fakeEntities.valueForAnnotation(annotationType);
          serverAnnotation = fakeEntities.annotation(value, annotationType);

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

          value = fakeEntities.valueForAnnotation(annotationType);
          serverAnnotation = fakeEntities.annotation(value, annotationType);

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

            if (annotationTypeType === ParticipantAnnotationType) {
              annotationTypeOptions.required = true;
            }

            annotationType = createAnnotationType(annotationTypeOptions);
            serverAnnotation = fakeEntities.annotation('', annotationType);
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
              serverAnnotation,
              annotation;

          annotTypeOptions = annotTypeOptions || {};

          annotationType   = createAnnotationType(annotTypeOptions);
          value            = fakeEntities.valueForAnnotation(annotationType);
          serverAnnotation = fakeEntities.annotation(value, annotationType);
          annotation       = createAnnotation(serverAnnotation, annotationType);

          return {
            annotationType:   annotationType,
            serverAnnotation: serverAnnotation,
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
