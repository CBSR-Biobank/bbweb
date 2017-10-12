/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('AnnotationTypeViewer', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin);
      this.injectDependencies('$uibModal',
                              'Study',
                              'AnnotationType',
                              'AnnotationValueType',
                              'AnnotationTypeViewer',
                              'Factory',
                              'TestUtils');

      this.study = new this.Study(this.Factory.study());

      this.annotatationTypeOptions = [
        { valueType: this.AnnotationValueType.TEXT      },
        { valueType: this.AnnotationValueType.NUMBER    },
        { valueType: this.AnnotationValueType.DATE_TIME },
        { valueType: this.AnnotationValueType.SELECT,   maxValueCount: 1 },
        { valueType: this.AnnotationValueType.SELECT,   maxValueCount: 2 }
      ];
    });
  });

  it('should open a modal when created', function () {
    var self = this,
        count = 0;

    spyOn(self.$uibModal, 'open').and.returnValue(this.TestUtils.fakeModal());

    this.annotatationTypeOptions.forEach((options) => {
      // jshint unused:false
      var annotationType = new self.AnnotationType(self.Factory.annotationType(options)),
          viewer = new self.AnnotationTypeViewer(annotationType);
      count++;
      expect(self.$uibModal.open.calls.count()).toBe(count);
    });
  });

  it('should throw an error when created when it has no options', function() {
    var self = this,
        annotationType = new this.AnnotationType(
          self.Factory.annotationType({
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

    self.annotatationTypeOptions.forEach((options) => {
      var annotationType, viewer, numAttributesExpected = 4;

      attributes = [];

      annotationType = new self.AnnotationType(self.Factory.annotationType(options));
      viewer = new self.AnnotationTypeViewer(annotationType);

      if (annotationType.isValueTypeSelect()) {
        numAttributesExpected += 1;
      }

      expect(attributes).toBeArrayOfSize(numAttributesExpected);

      attributes.forEach((attr) => {
        switch (attr.label) {
        case 'Name':
          expect(attr.value).toBe(annotationType.name);
          break;

        case 'Type':
          // in this case attr.value is a function that comes from annotationValueTypeLabelService
          expect(attr.value()).toBe(annotationType.getValueTypeLabelFunc()());
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
