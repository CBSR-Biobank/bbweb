/**
 * Jasmine test suite
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

    var scope,
        compile,
        element,
        AnnotationHelper,
        ParticipantAnnotationType,
        AnnotationValueType,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($compile,
                               $rootScope,
                               $templateCache,
                               _AnnotationHelper_,
                               _ParticipantAnnotationType_,
                               _AnnotationValueType_,
                               fakeDomainEntities) {
      compile = $compile;
      scope = $rootScope;
      AnnotationHelper           = _AnnotationHelper_;
      ParticipantAnnotationType  = _ParticipantAnnotationType_;
      AnnotationValueType        = _AnnotationValueType_;
      fakeEntities               = fakeDomainEntities;

      testUtils.putHtmlTemplate($templateCache,
                               '/assets/javascripts/common/directives/annotationsInput.html');
      element = angular.element(
        '<form name="form">' +
          '  <annotations-input annotation-helpers="model.annotationHelpers">' +
          '  </annotations-input>' +
          '</form>');
    }));

    function createAnnotationHelper(valueType) {
      return new AnnotationHelper(
        new ParticipantAnnotationType(
          fakeEntities.annotationType({ valueType: valueType, required: true })
        ));
    }

    function createScope(annotationHelpers) {
      scope.model = {
        annotationHelpers: annotationHelpers
      };
      compile(element)(scope);
      scope.$digest();
      return scope;
    }

    it('works for a TEXT annotation', function() {
      var annotationValue = fakeEntities.stringNext(),
          annotationHelpers = [ createAnnotationHelper(AnnotationValueType.TEXT()) ],
          scope = createScope(annotationHelpers);

      expect(element.find('input').length).toBe(1);
      expect(element.find('input').eq(0).attr('type')).toBe('text');
      scope.form.annotationSubForm.annotationValue.$setViewValue(annotationValue);
      expect(scope.model.annotationHelpers[0].getDisplayValue()).toBe(annotationValue);
      expect(scope.form.annotationSubForm.annotationValue.$valid).toBe(true);
    });

    it('works for a NUMBER annotation and a valid number', function() {
      var annotationValue = 111,
          annotationHelpers = [ createAnnotationHelper(AnnotationValueType.NUMBER()) ],
          scope = createScope(annotationHelpers);

      expect(element.find('input').length).toBe(1);
      expect(element.find('input').eq(0).attr('type')).toBe('number');
      scope.form.annotationSubForm.annotationValue.$setViewValue(annotationValue.toString());
      expect(scope.model.annotationHelpers[0].getDisplayValue()).toBe(annotationValue.toString());
      expect(scope.form.annotationSubForm.annotationValue.$valid).toBe(true);
    });

    it('validation fails for a NUMBER annotation and an invalid number', function() {
      var annotationValue = fakeEntities.stringNext(),
          annotationHelpers = [ createAnnotationHelper(AnnotationValueType.NUMBER()) ],
          scope = createScope(annotationHelpers);

      expect(element.find('input').length).toBe(1);
      expect(element.find('input').eq(0).attr('type')).toBe('number');
      scope.form.annotationSubForm.annotationValue.$setViewValue(annotationValue);
      expect(scope.model.annotationHelpers[0].getDisplayValue()).toBe(undefined);
      expect(scope.form.annotationSubForm.annotationValue.$valid).toBe(false);
    });

    /**
     * TODO: This test could be more thorough with regard to testing the time picker. Not sure how to do that
     * yet.
     */
    it('works for a DATE_TIME annotation and a valid number', function() {
      var dateTime = moment().local(),
          annotationValue = { date: dateTime.format(),
                              time: dateTime.format()
                            },
          annotationValueNoSeconds = moment(dateTime).set({
            'millisecond': 0,
            'second':      0,
            'minute':      dateTime.minutes(),
            'hour':        dateTime.hours()
          }),
          annotationHelpers = [ createAnnotationHelper(AnnotationValueType.DATE_TIME()) ],
          scope = createScope(annotationHelpers);

      expect(element.find('input').length).toBe(3); // 2 others are for time picker
      expect(element.find('input').eq(0).attr('type')).toBe('text');

      scope.form.annotationSubForm.annotationValue.$setViewValue(annotationValue.date);
      expect(scope.model.annotationHelpers[0].getDisplayValue())
        .toBe(annotationValueNoSeconds.format());
      expect(scope.form.annotationSubForm.annotationValue.$valid).toBe(true);
    });

    it('works for a SELECT single annotation annotation', function() {
      var annotationType, annotationHelper, scope;

      annotationType = new ParticipantAnnotationType(
        fakeEntities.annotationType({
          valueType: AnnotationValueType.SELECT(),
          maxValueCount: 1,
          options: [ 'option1', 'option2' ],
          required: true }));

      annotationHelper = new AnnotationHelper(annotationType);

      scope = createScope([ annotationHelper ]);

      expect(element.find('select').length).toBe(1);

      // number of options is the number of annotationType options plus one for the '-- make a selection --'
      // option
      expect(element.find('select option').length).toBe(annotationType.options.length + 1);

      _.each(annotationType.options, function (option) {
        scope.form.annotationSubForm.annotationValue.$setViewValue(option);
        expect(scope.model.annotationHelpers[0].getDisplayValue()).toBe(option);
        expect(scope.form.annotationSubForm.annotationValue.$valid).toBe(true);
      });
    });

    it('works for a SELECT multiple annotation', function() {
      var annotationType, annotationHelper, scope;

      annotationType = new ParticipantAnnotationType(
        fakeEntities.annotationType({
          valueType: AnnotationValueType.SELECT(),
          maxValueCount: 2,
          options: [ 'option1', 'option2', 'option3' ],
          required: true }));

      annotationHelper = new AnnotationHelper(annotationType);

      scope = createScope([ annotationHelper ]);

      // has the right number of check boxes
      expect(element.find('input').length).toBe(3);

      expect(element.find('label span').eq(0)).toHaveText(annotationType.options[0]);
      expect(element.find('label span').eq(1)).toHaveText(annotationType.options[1]);
      expect(element.find('label span').eq(2)).toHaveText(annotationType.options[2]);
    });

    it('selecting and unselecting an option for a SELECT multiple makes the form invalid', function() {
      var annotationType, annotationHelper, scope;

      annotationType = new ParticipantAnnotationType(
        fakeEntities.annotationType({
          valueType: AnnotationValueType.SELECT(),
          maxValueCount: 2,
          options: [ 'option1', 'option2', 'option3' ],
          required: true }));

      annotationHelper = new AnnotationHelper(annotationType);

      scope = createScope([ annotationHelper ]);

      // has the right number of check boxes
      expect(element.find('input').length).toBe(3);

      _.each(_.range(3), function (inputNum) {
        element.find('input').eq(inputNum).click();
        expect(scope.form.annotationSubForm.annotationValue.$valid).toBe(true);
        element.find('input').eq(inputNum).click();
        expect(scope.form.annotationSubForm.annotationValue.$valid).toBe(false);
      });
    });

  });

});
