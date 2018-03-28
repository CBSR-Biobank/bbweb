/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _           from 'lodash';
import ngModule    from '../../index'  // needs components from common module for this test suite

describe('annotationsInputModule', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function (TestSuiteMixin) {
      Object.assign(this, TestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              'annotationFactory',
                              'AnnotationType',
                              'AnnotationValueType',
                              'AnnotationMaxValueCount',
                              'Factory');
      this.createAnnotation = (valueType) =>
        this.annotationFactory.create(
          undefined,
          new this.AnnotationType(
            this.Factory.annotationType({ valueType: valueType, required: true })
          ));

      this.createController = (annotations) => {
        this.element = angular.element([
          '<form name="form">',
          '  <annotations-input annotations="vm.annotations">',
          '  </annotations-input>',
          '</form>'
        ].join(''));

        this.scope = this.$rootScope.$new();
        this.scope.vm = { annotations: annotations };
        this.$compile(this.element)(this.scope);
        this.scope.$digest();
        this.controller = this.element.controller('annotationsInput');
      };

    });
  });

  it('works for a TEXT annotation', function() {
    const annotationValue = this.Factory.stringNext(),
          annotations = [ this.createAnnotation(this.AnnotationValueType.TEXT) ];

    this.createController(annotations);
    expect(this.element.find('input').length).toBe(1);
    expect(this.element.find('input').eq(0).attr('type')).toBe('text');
    this.scope.form.annotationSubForm.annotationTextValue.$setViewValue(annotationValue);
    expect(this.scope.vm.annotations[0].value).toBe(annotationValue);
    expect(this.scope.form.annotationSubForm.annotationTextValue.$valid).toBe(true);
  });

  it('works for a NUMBER annotation and a valid number', function() {
    const annotationValue = 123.01,
          annotations = [ this.createAnnotation(this.AnnotationValueType.NUMBER) ];

    this.createController(annotations);
    expect(this.element.find('input').length).toBe(1);
    expect(this.element.find('input').eq(0).attr('type')).toBe('number');
    this.scope.form.annotationSubForm.annotationNumberValue.$setViewValue(annotationValue.toString());
    expect(this.scope.vm.annotations[0].value).toBe(annotationValue);
    expect(this.scope.form.annotationSubForm.annotationNumberValue.$valid).toBe(true);
  });

  it('validation fails for a NUMBER annotation and an invalid number', function() {
    const annotationValue = this.Factory.stringNext(),
          annotations = [ this.createAnnotation(this.AnnotationValueType.NUMBER) ];

    this.createController(annotations);
    expect(this.element.find('input').length).toBe(1);
    expect(this.element.find('input').eq(0).attr('type')).toBe('number');
    this.scope.form.annotationSubForm.annotationNumberValue.$setViewValue(annotationValue);
    expect(this.scope.vm.annotations[0].value).toBeUndefined();
    expect(this.scope.form.annotationSubForm.annotationNumberValue.$valid).toBe(false);
  });

  it('works for a DATE_TIME annotation and a valid date', function() {
    const dateStr = '2010-01-10 00:00',
          annotation = this.createAnnotation(this.AnnotationValueType.DATE_TIME),
          annotations = [ annotation ];

    this.createController(annotations);
    expect(this.element.find('input').length).toBe(1);
    expect(this.element.find('input').eq(0).attr('type')).toBe('text');

    this.scope.form.annotationSubForm.dateTimePickerSubForm.date.$setViewValue(dateStr);

    expect(this.scope.vm.annotations[0].getValue()).toBe(dateStr);
    expect(this.scope.form.annotationSubForm.dateTimePickerSubForm.$valid).toBe(true);
  });

  it('works for a SELECT single annotation', function() {
    const annotationType = new this.AnnotationType(
      this.Factory.annotationType({
        valueType:     this.AnnotationValueType.SELECT,
        maxValueCount: this.AnnotationMaxValueCount.SELECT_SINGLE,
        options:       [ 'option1', 'option2' ],
        required:      true
      }));

    const annotations = [ this.annotationFactory.create(undefined, annotationType) ];

    this.createController(annotations);
    expect(this.element.find('select').length).toBe(1);

    // number of options is the number of annotationType options plus one for the '-- make a selection --'
    // option
    expect(this.element.find('select option').length).toBe(annotationType.options.length + 1);

    annotationType.options.forEach((option) => {
      this.scope.form.annotationSubForm.annotationSelectValue.$setViewValue(option);
      expect(this.scope.vm.annotations[0].value).toBe(option);
      expect(this.scope.form.annotationSubForm.annotationSelectValue.$valid).toBe(true);
    });
  });

  it('works for a SELECT multiple annotation', function() {
    const annotationType = new this.AnnotationType(
      this.Factory.annotationType({
        valueType: this.AnnotationValueType.SELECT,
        maxValueCount: this.AnnotationMaxValueCount.SELECT_MULTIPLE,
        options: [ 'option1', 'option2', 'option3' ],
        required: true }));

    const annotation = this.annotationFactory.create(undefined, annotationType);

    this.createController([ annotation ]);

    // has the right number of check boxes
    expect(this.element.find('input').length).toBe(3);

    expect(this.element.find('label span').eq(0)).toHaveText(annotationType.options[0]);
    expect(this.element.find('label span').eq(1)).toHaveText(annotationType.options[1]);
    expect(this.element.find('label span').eq(2)).toHaveText(annotationType.options[2]);
  });

  // For a required SELECT MULTIPLE annotation type
  it('selecting and unselecting an option for a required SELECT MULTIPLE makes the form invalid',
     function() {
       const annotationType = new this.AnnotationType(
         this.Factory.annotationType({
           valueType:     this.AnnotationValueType.SELECT,
           maxValueCount: this.AnnotationMaxValueCount.SELECT_MULTIPLE,
           options:       [ 'option1', 'option2', 'option3' ],
           required:      true
         }));

       const annotation = this.annotationFactory.create(undefined, annotationType);

       this.createController([ annotation ]);

       // has the right number of check boxes
       expect(this.element.find('input').length).toBe(annotationType.options.length);

       _.range(annotationType.options.length).forEach((inputNum) => {
         this.element.find('input').eq(inputNum).click();
         expect(this.scope.form.annotationSubForm.annotationSelectValue.$valid).toBe(true);
         this.element.find('input').eq(inputNum).click();
         expect(this.scope.form.annotationSubForm.annotationSelectValue.$valid).toBe(false);
       });
     });

});
