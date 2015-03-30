// Jasmine test suite
//
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobank.testUtils',
  'biobankApp'
], function(angular,
            mocks,
            _,
            testUtils) {
  'use strict';

  describe('AnnotationTypeViewer', function() {

    var Study,
        ParticipantAnnotationType,
        CollectionEventAnnotationType,
        SpecimenLinkAnnotationType,
        AnnotationValueType,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_Study_,
                               _ParticipantAnnotationType_,
                               _CollectionEventAnnotationType_,
                               _SpecimenLinkAnnotationType_,
                               _AnnotationValueType_,
                               fakeDomainEntities) {
      Study                         = _Study_;
      ParticipantAnnotationType     = _ParticipantAnnotationType_;
      CollectionEventAnnotationType = _CollectionEventAnnotationType_;
      SpecimenLinkAnnotationType    = _SpecimenLinkAnnotationType_;
      AnnotationValueType           = _AnnotationValueType_;
      fakeEntities                  = fakeDomainEntities;
    }));

    describe('for Participant Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        var study = new Study(fakeEntities.study());

        context.annotationTypeType = ParticipantAnnotationType;
        context.createAnnotationType = function (options) {
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
          return new SpecimenLinkAnnotationType(
            fakeEntities.studyAnnotationType(study, options));
        };

      });

      sharedBehaviour(context);
    });

    function sharedBehaviour(context) {

      var modal, AnnotationTypeViewer, annotationTypeType, createAnnotationType, annotatationTypeOptions;

      describe('(shared)', function() {

        beforeEach(inject(function($modal, _AnnotationTypeViewer_) {
          modal                = $modal;
          AnnotationTypeViewer = _AnnotationTypeViewer_;

          annotationTypeType = context.annotationTypeType;
          createAnnotationType = context.createAnnotationType;

          annotatationTypeOptions = [
            { valueType: AnnotationValueType.TEXT()      },
            { valueType: AnnotationValueType.NUMBER()    },
            { valueType: AnnotationValueType.DATE_TIME() },
            { valueType: AnnotationValueType.SELECT(),   maxValueCount: 1 },
            { valueType: AnnotationValueType.SELECT(),   maxValueCount: 2 }
          ];
        }));

        it('should open a modal when created', function() {
          var count = 0;

          spyOn(modal, 'open').and.callFake(function () { return testUtils.fakeModal(); });

          _.each(annotatationTypeOptions, function (options) {
            // jshint unused:false
            var annotationType = createAnnotationType(options);
            var viewer = new AnnotationTypeViewer(annotationType);

            count++;
            expect(modal.open.calls.count()).toBe(count);
          });
        });

        it('should throw an error when created when it has no options', function() {
          var annotationType = createAnnotationType({
            valueType: AnnotationValueType.SELECT(),
            options: []
          });
          expect(function () { return new AnnotationTypeViewer(annotationType); })
            .toThrow(new Error('invalid annotation type options'));
        });

        it('should display valid attributes for all value types', function() {
          var EntityViewer = this.$injector.get('EntityViewer');
          var attributes = [];

          spyOn(EntityViewer.prototype, 'addAttribute').and.callFake(function (label, value) {
            attributes.push({label: label, value: value});
          });

          _.each(annotatationTypeOptions, function (options) {
            var annotationType, viewer, numAttributesExpected = 3;

            if (annotationTypeType === ParticipantAnnotationType) {
              numAttributesExpected++;
            }

            attributes = [];

            annotationType = createAnnotationType(options);
            viewer = new AnnotationTypeViewer(annotationType);

            if (annotationType.isValueTypeSelect()) {
              numAttributesExpected += 2;
            }

            expect(attributes).toBeArrayOfSize(numAttributesExpected);

            _.each(attributes, function(attr) {
              switch (attr.label) {
              case 'Name':
                expect(attr.value).toBe(annotationType.name);
                break;

              case 'Type':
                expect(attr.value).toBe(annotationType.valueType);
                break;

              case 'Required':
                if (!_.isUndefined(annotationType.required)) {
                  expect(attr.value).toBe(annotationType.required ? 'Yes' : 'No');
                } else {
                  jasmine.getEnv().fail('annotation type does not have a required attribute');
                }
                break;

              case 'Selections Allowed':
                if (annotationType.valueType === AnnotationValueType.SELECT()) {
                  expect(attr.value).toBe(annotationType.maxValueCount === 1 ? 'Single' : 'Multiple');
                } else {
                  jasmine.getEnv().fail('not a select annotation type' + annotationType.valueType);
                }
                break;

              case 'Selections':
                if (annotationType.valueType === AnnotationValueType.SELECT()) {
                  expect(attr.value).toBe(annotationType.options.join(', '));
                } else {
                  jasmine.getEnv().fail('not a select annotation type' + annotationType.valueType);
                }
                break;

              case 'Description':
                expect(attr.value).toBe(annotationType.description);
                break;

              default:
                jasmine.getEnv().fail('label is invalid: ' + attr.label);
              }
            });
          });
        });

      });

    }

  });

});
