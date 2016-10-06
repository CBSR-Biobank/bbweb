/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'moment',
  'biobankApp'
], function(angular, mocks, _, moment) {
  'use strict';

  describe('annotationsInputModule', function() {

    var createAnnotation = function (valueType) {
      return this.annotationFactory.create(
        undefined,
        new this.AnnotationType(
          this.factory.annotationType({ valueType: valueType, required: true })
        ));
    };

    var createController = function (annotations) {
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

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, testSuiteMixin, testUtils) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              'annotationFactory',
                              'AnnotationType',
                              'AnnotationValueType',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/common/annotationsInput/annotationsInput.html',
        '/assets/javascripts/common/annotationsInput/dateTimeAnnotation.html',
        '/assets/javascripts/common/annotationsInput/multipleSelectAnnotation.html',
        '/assets/javascripts/common/annotationsInput/numberAnnotation.html',
        '/assets/javascripts/common/annotationsInput/singleSelectAnnotation.html',
        '/assets/javascripts/common/annotationsInput/textAnnotation.html',
        '/assets/javascripts/common/components/dateTimePicker/dateTimePicker.html');
    }));

    it('works for a TEXT annotation', function() {
      var annotationValue = this.factory.stringNext(),
          annotations = [ createAnnotation.call(this, this.AnnotationValueType.TEXT) ];

      createController.call(this, annotations);
      expect(this.element.find('input').length).toBe(1);
      expect(this.element.find('input').eq(0).attr('type')).toBe('text');
      this.scope.form.annotationSubForm.annotationTextValue.$setViewValue(annotationValue);
      expect(this.scope.vm.annotations[0].value).toBe(annotationValue);
      expect(this.scope.form.annotationSubForm.annotationTextValue.$valid).toBe(true);
    });

    it('works for a NUMBER annotation and a valid number', function() {
      var annotationValue = 123.01,
          annotations = [ createAnnotation.call(this, this.AnnotationValueType.NUMBER) ];

      createController.call(this, annotations);
      expect(this.element.find('input').length).toBe(1);
      expect(this.element.find('input').eq(0).attr('type')).toBe('number');
      this.scope.form.annotationSubForm.annotationNumberValue.$setViewValue(annotationValue.toString());
      expect(this.scope.vm.annotations[0].value).toBe(annotationValue);
      expect(this.scope.form.annotationSubForm.annotationNumberValue.$valid).toBe(true);
    });

    it('validation fails for a NUMBER annotation and an invalid number', function() {
      var annotationValue = this.factory.stringNext(),
          annotations = [ createAnnotation.call(this, this.AnnotationValueType.NUMBER) ];

      createController.call(this, annotations);
      expect(this.element.find('input').length).toBe(1);
      expect(this.element.find('input').eq(0).attr('type')).toBe('number');
      this.scope.form.annotationSubForm.annotationNumberValue.$setViewValue(annotationValue);
      expect(this.scope.vm.annotations[0].value).toBeUndefined();
      expect(this.scope.form.annotationSubForm.annotationNumberValue.$valid).toBe(false);
    });

    it('works for a DATE_TIME annotation and a valid date', function() {
      var dateStr = '2010-01-10 00:00',
          annotation = createAnnotation.call(this, this.AnnotationValueType.DATE_TIME),
          annotations = [ annotation ];

      createController.call(this, annotations);
      expect(this.element.find('input').length).toBe(1);
      expect(this.element.find('input').eq(0).attr('type')).toBe('text');

      this.scope.form.annotationSubForm.dateTimePickerSubForm.date.$setViewValue(dateStr);

      expect(this.scope.vm.annotations[0].getValue()).toBe(dateStr);
      expect(this.scope.form.annotationSubForm.dateTimePickerSubForm.$valid).toBe(true);
    });

    it('works for a SELECT single annotation annotation', function() {
      var self = this,
          annotationType, annotations;

      annotationType = new self.AnnotationType(
        self.factory.annotationType({
          valueType:     self.AnnotationValueType.SELECT,
          maxValueCount: 1,
          options:       [ 'option1', 'option2' ],
          required:      true
        }));

      annotations = [ self.annotationFactory.create(undefined, annotationType) ];

      createController.call(self, annotations);
      expect(self.element.find('select').length).toBe(1);

      // number of options is the number of annotationType options plus one for the '-- make a selection --'
      // option
      expect(self.element.find('select option').length).toBe(annotationType.options.length + 1);

      _.each(annotationType.options, function (option) {
        self.scope.form.annotationSubForm.annotationSelectValue.$setViewValue(option);
        expect(self.scope.vm.annotations[0].value).toBe(option);
        expect(self.scope.form.annotationSubForm.annotationSelectValue.$valid).toBe(true);
      });
    });

    it('works for a SELECT multiple annotation', function() {
      var annotationType, annotation;

      annotationType = new this.AnnotationType(
        this.factory.annotationType({
          valueType: this.AnnotationValueType.SELECT,
          maxValueCount: 2,
          options: [ 'option1', 'option2', 'option3' ],
          required: true }));

      annotation = this.annotationFactory.create(undefined, annotationType);

      createController.call(this, [ annotation ]);

      // has the right number of check boxes
      expect(this.element.find('input').length).toBe(3);

      expect(this.element.find('label span').eq(0)).toHaveText(annotationType.options[0]);
      expect(this.element.find('label span').eq(1)).toHaveText(annotationType.options[1]);
      expect(this.element.find('label span').eq(2)).toHaveText(annotationType.options[2]);
    });

    // For a required SELECT MULTIPLE annotation type
    it('selecting and unselecting an option for a required SELECT MULTIPLE makes the form invalid',
        function() {
          var self = this,
              annotationType, annotation;

          annotationType = new self.AnnotationType(
            self.factory.annotationType({
              valueType:     self.AnnotationValueType.SELECT,
              maxValueCount: 2,
              options:       [ 'option1', 'option2', 'option3' ],
              required:      true
            }));

          annotation = self.annotationFactory.create(undefined, annotationType);

          createController.call(self, [ annotation ]);

          // has the right number of check boxes
          expect(self.element.find('input').length).toBe(annotationType.options.length);

          _.each(_.range(annotationType.options.length), function (inputNum) {
            self.element.find('input').eq(inputNum).click();
            expect(self.scope.form.annotationSubForm.annotationSelectValue.$valid).toBe(true);
            self.element.find('input').eq(inputNum).click();
            expect(self.scope.form.annotationSubForm.annotationSelectValue.$valid).toBe(false);
          });
        });

  });

});
