// Jasmine test suite
//
define([
  'angular',
  'angularMocks',
  'underscore',
  'moment',
  'biobankApp'
], function(angular, mocks, _, moment) {
  'use strict';

  describe('AnnotationHelper', function() {

    var Study,
        ParticipantAnnotationType,
        CollectionEventAnnotationType,
        SpecimenLinkAnnotationType,
        AnnotationHelper,
        AnnotationValueType,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_Study_,
                               _ParticipantAnnotationType_,
                               _CollectionEventAnnotationType_,
                               _SpecimenLinkAnnotationType_,
                               _AnnotationHelper_,
                               _AnnotationValueType_,
                               fakeDomainEntities) {
      Study                         = _Study_;
      ParticipantAnnotationType     = _ParticipantAnnotationType_;
      CollectionEventAnnotationType = _CollectionEventAnnotationType_;
      SpecimenLinkAnnotationType    = _SpecimenLinkAnnotationType_;
      AnnotationHelper              = _AnnotationHelper_;
      AnnotationValueType           = _AnnotationValueType_;
      fakeEntities                  = fakeDomainEntities;
    }));

    describe('for Participant Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        var study = new Study(fakeEntities.study());

        context.annotationTypeType = ParticipantAnnotationType;
        context.createAnnotationType = function (valueType) {
          return new ParticipantAnnotationType(
            fakeEntities.studyAnnotationType(
              study,
              { valueType: valueType, required: true }));
        };

      });

      sharedBehaviour(context);
    });

    describe('for Collection Event Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        var study = new Study(fakeEntities.study());

        context.annotationTypeType = CollectionEventAnnotationType;
        context.createAnnotationType = function (valueType) {
          return new CollectionEventAnnotationType(
            fakeEntities.studyAnnotationType(
              study,
              { valueType: valueType }));
        };

      });

      sharedBehaviour(context);
    });

    describe('for Specimen Link Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        var study = new Study(fakeEntities.study());

        context.annotationTypeType = SpecimenLinkAnnotationType;
        context.createAnnotationType = function (valueType) {
          return new SpecimenLinkAnnotationType(
            fakeEntities.studyAnnotationType(
              study,
              { valueType: valueType }));
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

        function createAnnotationHelper(annotationType) {
          return new AnnotationHelper(annotationType, getRequired());
        }


        it('when constructor "required" parameter omitted', function() {
          var annotationType = createAnnotationType(AnnotationValueType.TEXT());

          if (annotationTypeType === ParticipantAnnotationType) {
            expect(function () { return new AnnotationHelper(annotationType); })
              .not.toThrow(new Error('required not assigned'));
          } else {
            expect(function () { return new AnnotationHelper(annotationType); })
              .toThrow(new Error('required not assigned'));
          }
        });

        it('constructor fails if value type is invalid', function() {
          var annotationType = createAnnotationType('ABCDEF');
          expect(function () { return createAnnotationHelper(annotationType); })
            .toThrow(new Error('value type is invalid: ABCDEF'));
        });

        it('constructor fails if max value count is invalid', function() {
          var annotationType = createAnnotationType(AnnotationValueType.SELECT());
          annotationType.maxValueCount = 0;
          expect(function () { return createAnnotationHelper(annotationType); })
            .toThrow(new Error('invalid value for max count'));

          annotationType.maxValueCount = 3;
          expect(function () { return createAnnotationHelper(annotationType); })
            .toThrow(new Error('invalid value for max count'));
        });

        it('valid value attribute for Text, Number, and Select single annotation type', function() {
          var annotatationValueTypes = [
            AnnotationValueType.TEXT(),
            AnnotationValueType.NUMBER(),
            AnnotationValueType.SELECT()
          ];

          _.each(annotatationValueTypes, function (valueType) {
            var annotationType = createAnnotationType(valueType);
            var helper = createAnnotationHelper(annotationType);
            expect(helper.value).not.toBeDefined();
          });
        });

        it('valid value attributes for DateTime annotation type', function() {
          var annotationType = createAnnotationType(AnnotationValueType.DATE_TIME());

          var helper = createAnnotationHelper(annotationType);
          expect(helper.value.date).toBe(moment().format('YYYY-MM-DD'));
          expect(helper.value.time.format('YYYY-MM-DD h:mm A'))
            .toBe(moment().format('YYYY-MM-DD h:mm A'));
        });

        it('valid values attribute for Select annotation type', function() {
          var annotationType = createAnnotationType(AnnotationValueType.SELECT());
          annotationType.maxValueCount = 2;

          expect(annotationType.options).not.toBeEmptyArray();

          var helper = createAnnotationHelper(annotationType);
          expect(helper.values).toBeArrayOfObjects();
          expect(helper.values).toBeArrayOfSize(annotationType.options.length);

          var valueNames = _.pluck(helper.values, 'name');

          _.each(annotationType.options, function (option) {
            expect(valueNames).toContain(option);
          });

          _.each(helper.values, function(selectValue){
            expect(selectValue.checked).toBeFalse();
          });
        });

        it('calling getAnnotationTypeId gives a valid result', function() {
          var annotationType = createAnnotationType(AnnotationValueType.TEXT());

          var helper = createAnnotationHelper(annotationType);
          expect(helper.getAnnotationTypeId()).toBe(annotationType.id);
        });

        it('calling getLabel returns the annotation types name', function() {
          var annotationType = createAnnotationType(AnnotationValueType.TEXT());

          var helper = createAnnotationHelper(annotationType);
          expect(helper.getLabel()).toBe(annotationType.name);
        });

        it('calling setValue on Text annotation type sets the value correctly', function() {
          var annotationType = createAnnotationType(AnnotationValueType.TEXT());

          var helper = createAnnotationHelper(annotationType);
          var value = fakeEntities.domainEntityNameNext(fakeEntities.ENTITY_NAME_ANNOTATION_TYPE());
          helper.setValue({ stringValue: value });
          expect(helper.value).toBe(value);
          expect(helper.displayValue).toBe(value);
        });

        it('calling setValue on Number annotation type sets the value correctly', function() {
          var annotationType = createAnnotationType(AnnotationValueType.NUMBER());

          var helper = createAnnotationHelper(annotationType);
          var value = 123.456;
          helper.setValue({ numberValue: value.toString() });
          expect(helper.value).toBe(value);
          expect(helper.displayValue).toBe(value);
        });

        it('calling setValue on DateTime annotation type sets the value correctly', function() {
          var annotationType = createAnnotationType(AnnotationValueType.DATE_TIME());

          var helper = createAnnotationHelper(annotationType);
          var date = moment('2000-01-01T09:00:00-0700');
          var value = date.format();
          helper.setValue({ stringValue: value });
          expect(helper.value.date).toBe(date.format('YYYY-MM-DD'));
          expect(helper.value.time.format('h:mm A')).toEqual(date.format('h:mm A'));
          expect(helper.displayValue).toStartWith(date.format('YYYY-MM-DD h:mm A'));
        });

        it('getAnnotation returns a valid value for a Text annotation type', function() {
          var annotationType = createAnnotationType(AnnotationValueType.TEXT());

          var helper = createAnnotationHelper(annotationType);
          var value = fakeEntities.stringNext();
          helper.setValue({ stringValue: value });

          var annotation = helper.getAnnotation();
          expect(annotation.annotationTypeId).toEqual(annotationType.id);
          expect(annotation.selectedValues).toBeEmptyArray();
          expect(annotation.stringValue).toBe(value);
        });

        it('getAnnotation returns a valid value for a Number annotation type', function() {
          var annotationType = createAnnotationType(AnnotationValueType.NUMBER());

          var helper = createAnnotationHelper(annotationType);
          var value = 123.456;
          helper.setValue({ numberValue: value.toString() });

          var annotation = helper.getAnnotation();
          expect(annotation.annotationTypeId).toEqual(annotationType.id);
          expect(annotation.selectedValues).toBeEmptyArray();
          expect(annotation.numberValue).toBe(value.toString());
        });

        it('getAnnotation returns a valid value for a DateTime annotation type', function() {
          var annotationType = createAnnotationType(AnnotationValueType.DATE_TIME());

          var helper = createAnnotationHelper(annotationType);
          var date = moment('2000-01-01T09:00:00-0700');
          var value = date.format();
          helper.setValue({ stringValue: value });

          var annotation = helper.getAnnotation();
          expect(annotation.annotationTypeId).toEqual(annotationType.id);
          expect(annotation.selectedValues).toBeEmptyArray();
          expect(annotation.stringValue).toBe(value);
        });

        it('getAnnotation returns null for a Select single annotation type', function() {
          var annotationType = createAnnotationType(AnnotationValueType.SELECT());

          var helper = createAnnotationHelper(annotationType);
          helper.setValue({
            selectedValues: [ { value: annotationType.options[0] } ]
          });

          var annotation = helper.getAnnotation();
          expect(annotation.annotationTypeId).toBe(annotationType.id);
          expect(annotation.selectedValues).toBeArrayOfSize(1);
          expect(annotation.selectedValues[0].annotationTypeId).toBe(annotationType.id);
          expect(annotation.selectedValues[0].value).toBe(annotationType.options[0]);
          expect(annotation.stringValue).toBeUndefined();
        });

        it('getAnnotation returns null for a Select multiple annotation type', function() {
          var annotationType = createAnnotationType(AnnotationValueType.SELECT());
          annotationType.maxValueCount = 2;

          var helper = createAnnotationHelper(annotationType);
          helper.setValue({
            selectedValues: [
              { value: annotationType.options[0] },
              { value: annotationType.options[1] }
            ]
          });

          var annotation = helper.getAnnotation();
          expect(annotation.annotationTypeId).toBe(annotationType.id);
          expect(annotation.selectedValues).toBeArrayOfSize(2);
          expect(annotation.stringValue).toBeUndefined();

          expect(annotation.selectedValues[0].annotationTypeId).toBe(annotationType.id);
          expect(annotation.selectedValues[0].value).toBe(annotationType.options[0]);

          expect(annotation.selectedValues[1].annotationTypeId).toBe(annotationType.id);
          expect(annotation.selectedValues[1].value).toBe(annotationType.options[1]);
        });

      });

    }

  });


});
