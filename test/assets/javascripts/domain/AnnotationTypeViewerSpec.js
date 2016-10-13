/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define([
  'angular',
  'angularMocks',
  'lodash',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('AnnotationTypeViewer', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin.prototype);
      this.injectDependencies('$uibModal',
                              'Study',
                              'AnnotationType',
                              'AnnotationValueType',
                              'AnnotationTypeViewer',
                              'factory',
                              'testUtils');

      this.study = new this.Study(this.factory.study());

      this.annotatationTypeOptions = [
        { valueType: this.AnnotationValueType.TEXT      },
        { valueType: this.AnnotationValueType.NUMBER    },
        { valueType: this.AnnotationValueType.DATE_TIME },
        { valueType: this.AnnotationValueType.SELECT,   maxValueCount: 1 },
        { valueType: this.AnnotationValueType.SELECT,   maxValueCount: 2 }
      ];
    }));

    it('should open a modal when created', function () {
      var self = this,
          count = 0;

      spyOn(self.$uibModal, 'open').and.returnValue(this.testUtils.fakeModal());

      _.each(this.annotatationTypeOptions, function (options) {
        // jshint unused:false
        var annotationType = new self.AnnotationType(self.factory.annotationType(options)),
            viewer = new self.AnnotationTypeViewer(annotationType);
        count++;
        expect(self.$uibModal.open.calls.count()).toBe(count);
      });
    });

    it('should throw an error when created when it has no options', function() {
      var self = this,
          annotationType = new this.AnnotationType(
            self.factory.annotationType({
              valueType: self.AnnotationValueType.SELECT,
              options: []
            }));

      expect(function () { return new self.AnnotationTypeViewer(annotationType); })
        .toThrow(new Error('invalid annotation type options'));
    });

    it('should display valid attributes for all value types', function() {
      var self = this,
          EntityViewer = this.$injector.get('EntityViewer'),
          attributes = [];

      spyOn(EntityViewer.prototype, 'addAttribute').and.callFake(function (label, value) {
        attributes.push({label: label, value: value});
      });

      _.each(self.annotatationTypeOptions, function (options) {
        var annotationType, viewer, numAttributesExpected = 4;

        attributes = [];

        annotationType = new self.AnnotationType(self.factory.annotationType(options));
        viewer = new self.AnnotationTypeViewer(annotationType);

        if (annotationType.isValueTypeSelect()) {
          numAttributesExpected += 1;
        }

        expect(attributes).toBeArrayOfSize(numAttributesExpected);

        _.each(attributes, function(attr) {
          switch (attr.label) {
          case 'Name':
            expect(attr.value).toBe(annotationType.name);
            break;

          case 'Type':
            expect(attr.value).toBe(annotationType.getType());
            break;

          case 'Required':
            if (!_.isUndefined(annotationType.required)) {
              expect(attr.value).toBe(annotationType.required ? 'Yes' : 'No');
            } else {
              jasmine.getEnv().fail('annotation type does not have a required attribute');
            }
            break;

          case 'Selections Allowed':
            if (annotationType.valueType === self.AnnotationValueType.SELECT) {
              expect(attr.value).toBe(annotationType.maxValueCount === 1 ? 'Single' : 'Multiple');
            } else {
              jasmine.getEnv().fail('not a select annotation type' + annotationType.valueType);
            }
            break;

          case 'Selections':
            if (annotationType.valueType === self.AnnotationValueType.SELECT) {
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
