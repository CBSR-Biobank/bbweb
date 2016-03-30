/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'moment',
  'biobankApp'
], function(angular, mocks, _, moment) {
  'use strict';

  /**
   *
   */
  describe('annotationsInputModule', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, directiveTestSuite, testUtils) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.annotationFactory   = self.$injector.get('annotationFactory');
      self.AnnotationType      = self.$injector.get('AnnotationType');
      self.AnnotationValueType = self.$injector.get('AnnotationValueType');
      self.jsonEntities        = self.$injector.get('jsonEntities');

      self.createAnnotation = createAnnotation;
      self.createController = createController;

      self.putHtmlTemplates(
        '/assets/javascripts/common/annotationsInput/annotationsInput.html',
        '/assets/javascripts/common/annotationsInput/dateTimeAnnotation.html',
        '/assets/javascripts/common/annotationsInput/multipleSelectAnnotation.html',
        '/assets/javascripts/common/annotationsInput/numberAnnotation.html',
        '/assets/javascripts/common/annotationsInput/singleSelectAnnotation.html',
        '/assets/javascripts/common/annotationsInput/textAnnotation.html',
        '/assets/javascripts/common/directives/dateTime/dateTime.html');

      //--

      function createAnnotation(valueType) {
        return self.annotationFactory.create(
          undefined,
          new self.AnnotationType(
            self.jsonEntities.annotationType({ valueType: valueType, required: true })
          ));
      }

      function createController(annotations) {
        self.element = angular.element([
          '<form name="form">',
          '  <annotations-input annotations="vm.annotations">',
          '  </annotations-input>',
          '</form>'
        ].join(''));

        self.scope = $rootScope.$new();
        self.scope.vm = {
          annotations: annotations
        };
        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('annotationsInput');
      }
    }));

    it('works for a TEXT annotation', function() {
      var annotationValue = this.jsonEntities.stringNext(),
          annotations = [ this.createAnnotation(this.AnnotationValueType.TEXT()) ];

      this.createController(annotations);
      expect(this.element.find('input').length).toBe(1);
      expect(this.element.find('input').eq(0).attr('type')).toBe('text');
      this.scope.form.annotationSubForm.annotationTextValue.$setViewValue(annotationValue);
      expect(this.scope.vm.annotations[0].value).toBe(annotationValue);
      expect(this.scope.form.annotationSubForm.annotationTextValue.$valid).toBe(true);
    });

    it('works for a NUMBER annotation and a valid number', function() {
      var annotationValue = 111.01,
          annotations = [ this.createAnnotation(this.AnnotationValueType.NUMBER()) ];

      this.createController(annotations);
      expect(this.element.find('input').length).toBe(1);
      expect(this.element.find('input').eq(0).attr('type')).toBe('number');
      this.scope.form.annotationSubForm.annotationNumberValue.$setViewValue(annotationValue.toString());
      expect(this.scope.vm.annotations[0].value).toBe(annotationValue);
      expect(this.scope.form.annotationSubForm.annotationNumberValue.$valid).toBe(true);
    });

    it('validation fails for a NUMBER annotation and an invalid number', function() {
      var annotationValue = this.jsonEntities.stringNext(),
          annotations = [ this.createAnnotation(this.AnnotationValueType.NUMBER()) ];

      this.createController(annotations);
      expect(this.element.find('input').length).toBe(1);
      expect(this.element.find('input').eq(0).attr('type')).toBe('number');
      this.scope.form.annotationSubForm.annotationNumberValue.$setViewValue(annotationValue);
      expect(this.scope.vm.annotations[0].numberValue).toBe(null);
      expect(this.scope.form.annotationSubForm.annotationNumberValue.$valid).toBe(false);
    });

    /**
     * TODO: This test could be more thorough with regard to testing the time picker. Not sure how to do that
     * yet.
     */
    it('works for a DATE_TIME annotation and a valid number', function() {
      var timeService = this.$injector.get('timeService'),
          dateStr = '2010-01-10 12:00 PM',
          annotation = this.createAnnotation(this.AnnotationValueType.DATE_TIME()),
          annotations = [ annotation ];

      _.extend(annotation, timeService.stringToDateAndTime(dateStr));

      this.createController(annotations);
      expect(this.element.find('input').length).toBe(4); // 3 others are for time picker
      expect(this.element.find('input').eq(0).attr('type')).toBe('text');

      expect(this.scope.vm.annotations[0].getValue()).toBe(dateStr);
      expect(this.scope.form.annotationSubForm.dateTimeSubForm.dateValue.$valid).toBe(true);
    });

    it('works for a SELECT single annotation annotation', function() {
      var self = this,
          annotationType, annotations;

      annotationType = new self.AnnotationType(
        self.jsonEntities.annotationType({
          valueType:     self.AnnotationValueType.SELECT(),
          maxValueCount: 1,
          options:       [ 'option1', 'option2' ],
          required:      true
        }));

      annotations = [ self.annotationFactory.create(undefined, annotationType) ];

      self.createController(annotations);
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
        this.jsonEntities.annotationType({
          valueType: this.AnnotationValueType.SELECT(),
          maxValueCount: 2,
          options: [ 'option1', 'option2', 'option3' ],
          required: true }));

      annotation = this.annotationFactory.create(undefined, annotationType);

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
          var self = this,
              annotationType, annotation;

          annotationType = new self.AnnotationType(
            self.jsonEntities.annotationType({
              valueType:     self.AnnotationValueType.SELECT(),
              maxValueCount: 2,
              options:       [ 'option1', 'option2', 'option3' ],
              required:      true
            }));

          annotation = self.annotationFactory.create(undefined, annotationType);

          self.createController([ annotation ]);

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
