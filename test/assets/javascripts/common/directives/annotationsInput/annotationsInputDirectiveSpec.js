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

    var scope,
        compile,
        element,
        annotationFactory,
        ParticipantAnnotationType,
        AnnotationValueType,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($compile,
                               $rootScope,
                               $templateCache,
                               _annotationFactory_,
                               _ParticipantAnnotationType_,
                               _AnnotationValueType_,
                               fakeDomainEntities) {

      compile                   = $compile;
      scope                     = $rootScope;
      annotationFactory         = _annotationFactory_;
      ParticipantAnnotationType = _ParticipantAnnotationType_;
      AnnotationValueType       = _AnnotationValueType_;
      fakeEntities              = fakeDomainEntities;

      testUtils.putHtmlTemplate($templateCache,
                               '/assets/javascripts/common/directives/annotationsInput.html');
      element = angular.element(
        '<form name="form">' +
          '  <annotations-input annotations="model.annotations">' +
          '  </annotations-input>' +
          '</form>');
    }));

    function createAnnotation(valueType) {
      return annotationFactory.create(
        undefined,
        new ParticipantAnnotationType(
          fakeEntities.annotationType({ valueType: valueType, required: true })
        ));
    }

    function createScope(annotations) {
      scope.model = {
        annotations: annotations
      };
      compile(element)(scope);
      scope.$digest();
      return scope;
    }

    it('works for a TEXT annotation', function() {
      var annotationValue = fakeEntities.stringNext(),
          annotations = [ createAnnotation(AnnotationValueType.TEXT()) ],
          scope = createScope(annotations);

      expect(element.find('input').length).toBe(1);
      expect(element.find('input').eq(0).attr('type')).toBe('text');
      scope.form.annotationSubForm.annotationValue.$setViewValue(annotationValue);
      expect(scope.model.annotations[0].value).toBe(annotationValue);
      expect(scope.form.annotationSubForm.annotationValue.$valid).toBe(true);
    });

    it('works for a NUMBER annotation and a valid number', function() {
      var annotationValue = 111.01,
          annotations = [ createAnnotation(AnnotationValueType.NUMBER()) ],
          scope = createScope(annotations);

      expect(element.find('input').length).toBe(1);
      expect(element.find('input').eq(0).attr('type')).toBe('number');
      scope.form.annotationSubForm.annotationValue.$setViewValue(annotationValue.toString());
      expect(scope.model.annotations[0].value).toBe(annotationValue);
      expect(scope.form.annotationSubForm.annotationValue.$valid).toBe(true);
    });

    it('validation fails for a NUMBER annotation and an invalid number', function() {
      var annotationValue = fakeEntities.stringNext(),
          annotations = [ createAnnotation(AnnotationValueType.NUMBER()) ],
          scope = createScope(annotations);

      expect(element.find('input').length).toBe(1);
      expect(element.find('input').eq(0).attr('type')).toBe('number');
      scope.form.annotationSubForm.annotationValue.$setViewValue(annotationValue);
      expect(scope.model.annotations[0].numberValue).toBe(undefined);
      expect(scope.form.annotationSubForm.annotationValue.$valid).toBe(false);
    });

    /**
     * TODO: This test could be more thorough with regard to testing the time picker. Not sure how to do that
     * yet.
     */
    xit('works for a DATE_TIME annotation and a valid number', function() {
      var annotationValue = '2010-01-10 12:00 PM',
          annotations = [ createAnnotation(AnnotationValueType.DATE_TIME()) ],
          scope = createScope(annotations);

      expect(element.find('input').length).toBe(3); // 2 others are for time picker
      expect(element.find('input').eq(0).attr('type')).toBe('text');

      scope.form.annotationSubForm.annotationValue.$setViewValue(annotationValue);
      expect(scope.model.annotations[0].getValue()).toBe(annotationValue);
      expect(scope.form.annotationSubForm.annotationValue.$valid).toBe(true);
    });

    it('works for a SELECT single annotation annotation', function() {
      var annotationType, annotations, scope;

      annotationType = new ParticipantAnnotationType(
        fakeEntities.annotationType({
          valueType:     AnnotationValueType.SELECT(),
          maxValueCount: 1,
          options:       [ 'option1', 'option2' ],
          required:      true
        }));

      annotations = [ annotationFactory.create(undefined, annotationType) ];

      scope = createScope(annotations);

      expect(element.find('select').length).toBe(1);

      // number of options is the number of annotationType options plus one for the '-- make a selection --'
      // option
      expect(element.find('select option').length).toBe(annotationType.options.length + 1);

      _.each(annotationType.options, function (option) {
        scope.form.annotationSubForm.annotationValue.$setViewValue(option);
        expect(scope.model.annotations[0].value).toBe(option);
        expect(scope.form.annotationSubForm.annotationValue.$valid).toBe(true);
      });
    });

    it('works for a SELECT multiple annotation', function() {
      var annotationType, annotation, scope;

      annotationType = new ParticipantAnnotationType(
        fakeEntities.annotationType({
          valueType: AnnotationValueType.SELECT(),
          maxValueCount: 2,
          options: [ 'option1', 'option2', 'option3' ],
          required: true }));

      annotation = annotationFactory.create(undefined, annotationType);

      scope = createScope([ annotation ]);

      // has the right number of check boxes
      expect(element.find('input').length).toBe(3);

      expect(element.find('label span').eq(0)).toHaveText(annotationType.options[0]);
      expect(element.find('label span').eq(1)).toHaveText(annotationType.options[1]);
      expect(element.find('label span').eq(2)).toHaveText(annotationType.options[2]);
    });

    // For a required SELECT MULTIPLE annotation type
    it('selecting and unselecting an option for a SELECT MULTIPLE makes the form invalid', function() {
      var annotationType, annotation, scope;

      annotationType = new ParticipantAnnotationType(
        fakeEntities.annotationType({
          valueType:     AnnotationValueType.SELECT(),
          maxValueCount: 2,
          options:       [ 'option1', 'option2', 'option3' ],
          required:      true
        }));

      annotation = annotationFactory.create(undefined, annotationType);

      scope = createScope([ annotation ]);

      // has the right number of check boxes
      expect(element.find('input').length).toBe(annotationType.options.length);

      _.each(_.range(annotationType.options.length), function (inputNum) {
        element.find('input').eq(inputNum).click();
        expect(scope.form.annotationSubForm.annotationValue.$valid).toBe(true);
        element.find('input').eq(inputNum).click();
        expect(scope.form.annotationSubForm.annotationValue.$valid).toBe(false);
      });
    });

  });

});
