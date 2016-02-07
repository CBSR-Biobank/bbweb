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
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, moment, testUtils) {
  'use strict';

  /**
   *
   */
  describe('Directive: annotationsInput', function() {

    var element,
        scope,
        controller,
        createScope,
        annotationFactory,
        ParticipantAnnotationType,
        AnnotationValueType,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function() {
      var $templateCache = this.$injector.get('$templateCache');

      annotationFactory         = this.$injector.get('annotationFactory');
      ParticipantAnnotationType = this.$injector.get('ParticipantAnnotationType');
      AnnotationValueType       = this.$injector.get('AnnotationValueType');
      fakeEntities              = this.$injector.get('fakeDomainEntities');

      createScope = setupController(this.$injector);

      testUtils.putHtmlTemplates($templateCache,
                               '/assets/javascripts/common/directives/annotationsInput/annotationsInput.html');
    }));

    function createAnnotation(valueType) {
      return annotationFactory.create(
        undefined,
        new ParticipantAnnotationType(
          fakeEntities.annotationType({ valueType: valueType, required: true })
        ));
    }

    function setupController(injector) {
      var $rootScope = injector.get('$rootScope'),
          $compile   = injector.get('$compile');

      return create;

      //--

      function create(annotations) {
        element = angular.element([
          '<form name="form">',
          '  <annotations-input annotations="vm.annotations">',
          '  </annotations-input>',
          '</form>'
        ].join(''));

        scope = $rootScope.$new();
        scope.vm = {
          annotations: annotations
        };
        $compile(element)(scope);
        scope.$digest();
        return element.controller('annotationsInput');
      }
    }

    it('works for a TEXT annotation', function() {
      var annotationValue = fakeEntities.stringNext(),
          annotations = [ createAnnotation(AnnotationValueType.TEXT()) ];

      createScope(annotations);
      expect(element.find('input').length).toBe(1);
      expect(element.find('input').eq(0).attr('type')).toBe('text');
      scope.form.annotationSubForm.annotationTextValue.$setViewValue(annotationValue);
      expect(scope.vm.annotations[0].value).toBe(annotationValue);
      expect(scope.form.annotationSubForm.annotationTextValue.$valid).toBe(true);
    });

    it('works for a NUMBER annotation and a valid number', function() {
      var annotationValue = 111.01,
          annotations = [ createAnnotation(AnnotationValueType.NUMBER()) ];

      createScope(annotations);
      expect(element.find('input').length).toBe(1);
      expect(element.find('input').eq(0).attr('type')).toBe('number');
      scope.form.annotationSubForm.annotationNumberValue.$setViewValue(annotationValue.toString());
      expect(scope.vm.annotations[0].value).toBe(annotationValue);
      expect(scope.form.annotationSubForm.annotationNumberValue.$valid).toBe(true);
    });

    it('validation fails for a NUMBER annotation and an invalid number', function() {
      var annotationValue = fakeEntities.stringNext(),
          annotations = [ createAnnotation(AnnotationValueType.NUMBER()) ];

      createScope(annotations);
      expect(element.find('input').length).toBe(1);
      expect(element.find('input').eq(0).attr('type')).toBe('number');
      scope.form.annotationSubForm.annotationNumberValue.$setViewValue(annotationValue);
      expect(scope.vm.annotations[0].numberValue).toBe(undefined);
      expect(scope.form.annotationSubForm.annotationNumberValue.$valid).toBe(false);
    });

    /**
     * TODO: This test could be more thorough with regard to testing the time picker. Not sure how to do that
     * yet.
     */
    it('works for a DATE_TIME annotation and a valid number', function() {
      var timeService = this.$injector.get('timeService'),
          dateStr = '2010-01-10 12:00 PM',
          annotation = createAnnotation(AnnotationValueType.DATE_TIME()),
          annotations = [ annotation ];

      _.extend(annotation, timeService.stringToDateAndTime(dateStr));

      createScope(annotations);
      expect(element.find('input').length).toBe(4); // 3 others are for time picker
      expect(element.find('input').eq(0).attr('type')).toBe('text');

      expect(scope.vm.annotations[0].getValue()).toBe(dateStr);
      expect(scope.form.annotationSubForm.annotationDateTimeValue.$valid).toBe(true);
    });

    it('works for a SELECT single annotation annotation', function() {
      var annotationType, annotations;

      annotationType = new ParticipantAnnotationType(
        fakeEntities.annotationType({
          valueType:     AnnotationValueType.SELECT(),
          maxValueCount: 1,
          options:       [ 'option1', 'option2' ],
          required:      true
        }));

      annotations = [ annotationFactory.create(undefined, annotationType) ];

      createScope(annotations);
      expect(element.find('select').length).toBe(1);

      // number of options is the number of annotationType options plus one for the '-- make a selection --'
      // option
      expect(element.find('select option').length).toBe(annotationType.options.length + 1);

      _.each(annotationType.options, function (option) {
        scope.form.annotationSubForm.annotationSelectValue.$setViewValue(option);
        expect(scope.vm.annotations[0].value).toBe(option);
        expect(scope.form.annotationSubForm.annotationSelectValue.$valid).toBe(true);
      });
    });

    it('works for a SELECT multiple annotation', function() {
      var annotationType, annotation;

      annotationType = new ParticipantAnnotationType(
        fakeEntities.annotationType({
          valueType: AnnotationValueType.SELECT(),
          maxValueCount: 2,
          options: [ 'option1', 'option2', 'option3' ],
          required: true }));

      annotation = annotationFactory.create(undefined, annotationType);

      createScope([ annotation ]);

      // has the right number of check boxes
      expect(element.find('input').length).toBe(3);

      expect(element.find('label span').eq(0)).toHaveText(annotationType.options[0]);
      expect(element.find('label span').eq(1)).toHaveText(annotationType.options[1]);
      expect(element.find('label span').eq(2)).toHaveText(annotationType.options[2]);
    });

    // For a required SELECT MULTIPLE annotation type
    it('selecting and unselecting an option for a SELECT MULTIPLE makes the form invalid', function() {
      var annotationType, annotation;

      annotationType = new ParticipantAnnotationType(
        fakeEntities.annotationType({
          valueType:     AnnotationValueType.SELECT(),
          maxValueCount: 2,
          options:       [ 'option1', 'option2', 'option3' ],
          required:      true
        }));

      annotation = annotationFactory.create(undefined, annotationType);

      createScope([ annotation ]);

      // has the right number of check boxes
      expect(element.find('input').length).toBe(annotationType.options.length);

      _.each(_.range(annotationType.options.length), function (inputNum) {
        element.find('input').eq(inputNum).click();
        expect(scope.form.annotationSubForm.annotationSelectValue.$valid).toBe(true);
        element.find('input').eq(inputNum).click();
        expect(scope.form.annotationSubForm.annotationSelectValue.$valid).toBe(false);
      });
    });

  });

});
