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

    var modal, AnnotationTypeViewer, AnnotationValueType, fakeEntities;

    var annotatationTypeOptions;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($modal,
                               _AnnotationTypeViewer_,
                               _AnnotationValueType_,
                               fakeDomainEntities) {
      modal                = $modal;
      AnnotationTypeViewer = _AnnotationTypeViewer_;
      AnnotationValueType  = _AnnotationValueType_;
      fakeEntities         = fakeDomainEntities;

      annotatationTypeOptions = [
        { valueType: AnnotationValueType.TEXT()      },
        { valueType: AnnotationValueType.NUMBER()    },
        { valueType: AnnotationValueType.DATE_TIME() },
        { valueType: AnnotationValueType.SELECT(),   maxValueCount: 1 },
        { valueType: AnnotationValueType.SELECT(),   maxValueCount: 2 }
      ];
    }));

    it('should open a modal when created', function() {
      var self = this, count = 0;
      var modal = self.$injector.get('$modal');
      spyOn(modal, 'open').and.callFake(function () { return testUtils.fakeModal(); });

      _.each(annotatationTypeOptions, function (options) {
        // jshint unused:false
        var annotationType = fakeEntities.annotationType(options);
        var viewer = new AnnotationTypeViewer(annotationType);

        count++;
        expect(modal.open.calls.count()).toBe(count);
      });
    });

    it('should throw an error when created when it has no options', function() {
      var annotationType = fakeEntities.annotationType({
        valueType: AnnotationValueType.SELECT(),
        options: []
      });
      expect(function () { return new AnnotationTypeViewer(annotationType); })
        .toThrow(new Error('invalid annotation type options'));
    });

    it('should display valid attributes for a Text annotation', function() {
      var EntityViewer = this.$injector.get('EntityViewer');
      var attributes = [];

      spyOn(EntityViewer.prototype, 'addAttribute').and.callFake(function (label, value) {
        attributes.push({label: label, value: value});
      });

      // Participant annotation types have a "required" attribute, but collection event and specimen link type
      // annotation types do not. All 3 variants must be tested.

      _.each(annotatationTypeOptions, function (baseOptions) {
        _.each([true, false, undefined], function(required) {
          var annotationType, viewer;
          var options = {}, maybeRequired = {}, numAttributesExpected = 3;

          attributes = [];

          if (!_.isUndefined(required)) {
            numAttributesExpected++;
            maybeRequired.required = required;
          }

          _.extend(options, baseOptions, maybeRequired);

          annotationType = fakeEntities.annotationType(options);
          viewer = new AnnotationTypeViewer(annotationType);

          if (annotationType.valueType === AnnotationValueType.SELECT()) {
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


  });

});
