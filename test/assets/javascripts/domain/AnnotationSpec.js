/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
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
        Annotation,
        AnnotationValueType,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_Study_,
                               _ParticipantAnnotationType_,
                               _CollectionEventAnnotationType_,
                               _SpecimenLinkAnnotationType_,
                               _Annotation_,
                               _AnnotationValueType_,
                               fakeDomainEntities,
                               extendedDomainEntities) {
      Study                         = _Study_;
      ParticipantAnnotationType     = _ParticipantAnnotationType_;
      CollectionEventAnnotationType = _CollectionEventAnnotationType_;
      SpecimenLinkAnnotationType    = _SpecimenLinkAnnotationType_;
      Annotation                    = _Annotation_;
      AnnotationValueType           = _AnnotationValueType_;
      fakeEntities                  = fakeDomainEntities;

      testUtils.addCustomMatchers();
    }));

    describe('for Participant Annotation Types', function() {
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

    describe('for Collection Event Annotation Types', function() {
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

    describe('for Specimen Link Annotation Types', function() {
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
          return new Annotation(baseObj, annotationType, getRequired());
        }

        it('when constructor "required" parameter omitted', function() {
          var annotationType = createAnnotationType({
            valueType: AnnotationValueType.TEXT()
          });

          if (annotationTypeType === ParticipantAnnotationType) {
            expect(function () { return new Annotation({}, annotationType); })
              .not.toThrow(new Error('required not assigned'));
          } else {
            expect(function () { return new Annotation({}, annotationType); })
              .toThrow(new Error('required not assigned'));
          }
        });

        it('constructor fails if value type is invalid', function() {
          var annotationType = createAnnotationType({
            valueType: 'ABCDEF'
          });
          expect(function () { return createAnnotation({}, annotationType); })
            .toThrow(new Error('value type is invalid: ABCDEF'));
        });

        it('constructor fails if max value count is invalid', function() {
          var annotationType = createAnnotationType({
            valueType: AnnotationValueType.SELECT()
          });
          annotationType.maxValueCount = 0;
          expect(function () { return createAnnotation({}, annotationType); })
            .toThrow(new Error('invalid value for max count'));

          annotationType.maxValueCount = 3;
          expect(function () { return createAnnotation({}, annotationType); })
            .toThrow(new Error('invalid value for max count'));
        });

        it('fails when creating from a non object', function() {
          expect(Annotation.create(1))
            .toEqual(new Error('invalid object from server: has the correct keys'));
        });

        it('fails when creating from server response with bad selections', function() {
          var serverObj = {
            annotationTypeId: fakeEntities.stringNext(),
            selectedValues: { tmp: 1 }
          };
          expect(Annotation.create(serverObj))
            .toEqual(new Error('invalid selected values in object from server'));
        });

        it('has valid values when created from server response', function() {
          _.each(AnnotationValueType.values(), function (valueType) {
            var entities = getAnnotationAndType(valueType);
            var annotation = Annotation.create(entities.serverAnnotation,
                                               entities.annotationType,
                                               getRequired());
            annotation.compareToServerEntity(entities.serverAnnotation);
          });
        });

        it('has valid values when created from server response and is MULTIPLE SELECT', function() {
          var annotationType = createAnnotationType({
            valueType: AnnotationValueType.SELECT(),
            maxValueCount: 2,
            options: [ 'opt1', 'opt2', 'opt3' ]
          });
          var serverAnnotation = fakeEntities.annotation(
            fakeEntities.valueForAnnotation(annotationType),
            annotationType);
          var annotation = Annotation.create(serverAnnotation, annotationType, getRequired());
          expect(annotation.multipleSelectValues).toBeArrayOfSize(annotationType.options.length);
          expect(_.pluck(annotation.multipleSelectValues, 'name')).toContainAll(annotationType.options);
        });

        it('valid value attribute for Text, Date annotation type', function() {
          var annotatationValueTypes = [
            AnnotationValueType.TEXT(),
            AnnotationValueType.DATE_TIME()
          ];

          _.each(annotatationValueTypes, function (valueType) {
            var entities = getAnnotationAndType(valueType);
            expect(entities.annotation.stringValue).toBeDefined();
          });
        });

        it('valid value attributes for Number annotation type', function() {
          var entities = getAnnotationAndType(AnnotationValueType.NUMBER());
          expect(entities.annotation.numberValue).toBeDefined();
        });

        it('valid values attribute for Select annotation type', function() {
          var annotation,
              pluckedOptions,
              annotationType = createAnnotationType({
                valueType: AnnotationValueType.SELECT(),
                maxValueCount: 2
              });

          expect(annotationType.options).not.toBeEmptyArray();

          annotation = createAnnotation({}, annotationType);
          pluckedOptions = _.pluck(annotation.multipleSelectValues, 'name');
          expect(pluckedOptions).toBeArrayOfSize(annotationType.options.length);
          expect(pluckedOptions).toContainAll(annotationType.options);
        });

        it('calling getAnnotationTypeId gives a valid result', function() {
          var entities = getAnnotationAndType(AnnotationValueType.TEXT());
          expect(entities.annotation.getAnnotationTypeId())
            .toBe(entities.annotationType.id);
        });

        it('calling getValueType returns the annotation types value type', function() {
          var entities = getAnnotationAndType(AnnotationValueType.TEXT());
          expect(entities.annotation.getValueType())
            .toBe(entities.annotationType.valueType);
        });

        it('calling getLabel returns the annotation types name', function() {
          var entities = getAnnotationAndType(AnnotationValueType.TEXT());
          expect(entities.annotation.getLabel())
            .toBe(entities.annotationType.name);
        });

        it('calling isValid returns true if the annotation is not required', function() {
          _.each(AnnotationValueType.values(), function (valueType) {
            var annotationType = createAnnotationType({ valueType: valueType }),
                serverAnnotation = fakeEntities.annotation('', annotationType),
                annotation = new Annotation(serverAnnotation, annotationType, false);

            expect(annotation.isValid()).toBe(true);
          });
        });

        it('calling isValid returns FALSE if the annotation is required and has no value', function() {
          _.each(AnnotationValueType.values(), function (valueType) {
            var annotationType,
                annotationTypeOptions,
                serverAnnotation,
                annotation;

            annotationTypeOptions = { valueType: valueType };

            if (annotationTypeType === ParticipantAnnotationType) {
              annotationTypeOptions.required = true;
            }

            annotationType = createAnnotationType(annotationTypeOptions);
            serverAnnotation = fakeEntities.annotation(null, annotationType);
            annotation = new Annotation(serverAnnotation, annotationType, true);
            expect(annotation.isValid()).toBe(false);
          });
        });

        it('calling isValid returns TRUE if the annotation is required and has a value', function() {
          _.each(AnnotationValueType.values(), function (valueType) {
            var annotationType,
                annotationTypeOptions,
                value,
                serverAnnotation,
                annotation;

            annotationTypeOptions = { valueType: valueType };

            if (annotationTypeType === ParticipantAnnotationType) {
              annotationTypeOptions.required = true;
            }

            annotationType = createAnnotationType(annotationTypeOptions);
            value = fakeEntities.valueForAnnotation(annotationType);
            serverAnnotation = fakeEntities.annotation(value, annotationType);
            annotation = new Annotation(serverAnnotation, annotationType, true);
            expect(annotation.isValid()).toBe(true);
          });
        });

        it('getAnnotationTypeId throws an error when not initialized with an annotation type', function() {
          var serverObj = { annotationTypeId: fakeEntities.stringNext(),
                            selectedValues: []
                          },
              annotation = Annotation.create(serverObj);
          expect(function () { annotation.getAnnotationTypeId(); })
            .toThrow(new Error('annotation type not assigned'));
        });

        it('getValueType throws an error when not initialized with an annotation type', function() {
          var serverObj = { annotationTypeId: fakeEntities.stringNext(),
                            selectedValues: []
                          },
              annotation = Annotation.create(serverObj);
          expect(function () { annotation.getValueType(); })
            .toThrow(new Error('annotation type not assigned'));
        });

        it('getLabel throws an error when not initialized with an annotation type', function() {
          var serverObj = { annotationTypeId: fakeEntities.stringNext(),
                            selectedValues: []
                          },
              annotation = Annotation.create(serverObj);
          expect(function () { annotation.getLabel(); })
            .toThrow(new Error('annotation type not assigned'));
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

        it('getValue throws an error when not initialized with an annotation type', function() {
          var serverObj = { annotationTypeId: fakeEntities.stringNext(),
                            selectedValues: []
                          },
              annotation = Annotation.create(serverObj);
          expect(function () { annotation.getValue(); })
            .toThrow(new Error('annotation type not assigned'));
        });

        it('getServerAnnotation returns valid results for non select annotation types', function() {
          var annotationType,
              annotation,
              serverAnnotation,
              value,
              valueTypes = [
                AnnotationValueType.TEXT(),
                AnnotationValueType.NUMBER(),
                AnnotationValueType.DATE_TIME()
              ];

          _.each(valueTypes, function (valueType) {
            annotationType = createAnnotationType({ valueType: valueType });

            value = fakeEntities.valueForAnnotation(annotationType);
            serverAnnotation = fakeEntities.annotation(value, annotationType);

            annotation = createAnnotation(serverAnnotation, annotationType);
            expect(annotation.getServerAnnotation()).toEqual(serverAnnotation);
          });
        });

        it('getServerAnnotation returns valid results for single select', function() {
          var annotationType, annotation, serverAnnotation;

          annotationType = createAnnotationType({
            valueType:     AnnotationValueType.SELECT(),
            maxValueCount: 1,
            options:       [ 'option1', 'option2' ],
            required:      true
          });

          annotation = createAnnotation({}, annotationType);

          expect(annotationType.options).not.toBeEmptyArray();
          expect(annotation.multipleSelectValues).toBeEmptyArray();

          annotation.singleSelectValue = annotationType.options[0];

          serverAnnotation = annotation.getServerAnnotation();
          expect(serverAnnotation.selectedValues).toBeArrayOfSize(1);
          _.each(serverAnnotation.selectedValues, function (selectedValues) {
            expect(selectedValues.annotationTypeId).toEqual(annotationType.id);
            expect(annotationType.options).toContain(selectedValues.value);
          });
        });

        it('getServerAnnotation returns valid results for multiple select', function() {
          var annotationType, annotation, serverAnnotations;

          annotationType = createAnnotationType({
            valueType:     AnnotationValueType.SELECT(),
            maxValueCount: 2,
            options:       [ 'option1', 'option2' ],
            required:      true
          });

          annotation = createAnnotation({}, annotationType);

          expect(annotationType.options).not.toBeEmptyArray();
          expect(annotation.multipleSelectValues).toBeArrayOfSize(annotationType.options.length);

          annotation.multipleSelectValues[0].checked = true;

          serverAnnotations = annotation.getServerAnnotation();
          expect(serverAnnotations.selectedValues).toBeArrayOfSize(1);
          _.each(serverAnnotations.selectedValues, function (serverAnnotation) {
            expect(serverAnnotation.annotationTypeId).toEqual(annotationType.id);
            expect(annotationType.options).toContain(serverAnnotation.value);
          });
        });

        it('someSelected throws an error for an annotation type that is not MULTIPLE SELECT', function() {
          _.each(AnnotationValueType.values(), function (valueType) {
            var entities = getAnnotationAndType(valueType);
            var annotation = Annotation.create(entities.serverAnnotation,
                                               entities.annotationType,
                                               getRequired());
            expect(function () { annotation.someSelected(); })
              .toThrow(new Error('invalid select type: valueType: ' + entities.annotationType.valueType +
                        ' maxValueCount:' + entities.annotationType.maxValueCount));
          });
        });

        it('getServerAnnotation throws an error when not initialized with an annotation type', function() {
          var serverObj = { annotationTypeId: fakeEntities.stringNext(),
                            selectedValues: []
                          },
              annotation = Annotation.create(serverObj);
          expect(function () { annotation.getValue(); })
            .toThrow(new Error('annotation type not assigned'));
        });

        it('someSelected returns valid results for multiple select', function() {
          var annotationType, annotation;

          annotationType = createAnnotationType({
            valueType:     AnnotationValueType.SELECT(),
            maxValueCount: 2,
            options:       [ 'option1', 'option2' ],
            required:      true
          });

          annotation = createAnnotation({}, annotationType);

          expect(annotationType.options).not.toBeEmptyArray();
          expect(annotation.multipleSelectValues).toBeArrayOfSize(annotationType.options.length);
          expect(annotation.someSelected()).toBe(false);

          annotation.multipleSelectValues[0].checked = true;
          expect(annotation.someSelected()).toBe(true);
        });

        function getAnnotationAndType(valueType) {
          var annotationType = createAnnotationType({ valueType: valueType }),
              value = fakeEntities.valueForAnnotation(annotationType),
              serverAnnotation = fakeEntities.annotation(value, annotationType),
              annotation = createAnnotation(serverAnnotation, annotationType);

          return {
            annotationType:   annotationType,
            serverAnnotation: serverAnnotation,
            annotation:       annotation
          };
        }

      });

    }

  });

});
