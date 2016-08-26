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
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: annotationTypeAddDirective', function() {

    var createController = function () {
      this.element = angular.element([
        '<annotation-type-add',
        '  on-submit="vm.onSubmit"',
        '  on-cancel="vm.onCancel"',
        '</annotation-type-add>'
      ].join(''));

      this.scope = this.$rootScope.$new();
      this.scope.vm = {
        onSubmit: this.onSubmit,
        onCancel: this.onCancel
      };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('annotationTypeAdd');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (testUtils, factory, testSuiteMixin) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              'AnnotationType',
                              'AnnotationValueType',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/annotationTypeAdd/annotationTypeAdd.html');

      self.onSubmit = jasmine.createSpy('onSubmit');
      self.onCancel = jasmine.createSpy('onCancel');
    }));

    it('scope should be valid when adding', function() {
      createController.call(this);
      expect(this.controller.annotationType).toEqual(jasmine.any(this.AnnotationType));
      expect(this.controller.valueTypes).toEqual(_.values(this.AnnotationValueType));
    });

    it('maxValueCountRequired is valid', function() {
      createController.call(this);

      this.controller.annotationType.valueType = this.AnnotationValueType.SELECT;

      this.controller.annotationType.maxValueCount = 0;
      expect(this.controller.maxValueCountRequired()).toBe(true);

      this.controller.annotationType.maxValueCount = 3;
      expect(this.controller.maxValueCountRequired()).toBe(true);

      this.controller.annotationType.maxValueCount = 1;
      expect(this.controller.maxValueCountRequired()).toBe(false);

      this.controller.annotationType.maxValueCount = 2;
      expect(this.controller.maxValueCountRequired()).toBe(false);
    });

    it('calling valueTypeChange clears the options array', function() {
      createController.call(this);

      this.controller.annotationType.valueType = 'Select';
      this.controller.annotationType.maxValueCount = 1;

      this.controller.valueTypeChange();
      expect(this.controller.annotationType.options).toBeArray();
      expect(this.controller.annotationType.options).toBeEmptyArray();
    });

    it('calling optionAdd appends to the options array', function() {
      createController.call(this);

      this.controller.annotationType.valueType = 'Select';
      this.controller.annotationType.maxValueCount = 1;
      this.controller.annotationType.valueTypeChanged();

      this.controller.optionAdd();
      expect(this.controller.annotationType.options).toBeArrayOfSize(1);
      expect(this.controller.annotationType.options).toBeArrayOfStrings();
    });

    it('calling optionRemove throws an error on empty array', function() {
      createController.call(this);

      this.controller.annotationType.valueType = 'Select';
      this.controller.annotationType.maxValueCount = 1;
      this.controller.annotationType.valueTypeChanged();
      expect(function () { this.controller.optionRemove('abc'); }).toThrow();
    });

    it('calling optionRemove throws an error if removal results in empty array', function() {
      createController.call(this);

      this.controller.annotationType.valueType = 'Select';
      this.controller.annotationType.maxValueCount = 1;
      this.controller.annotationType.valueTypeChanged();
      this.controller.annotationType.options = ['abc'];
      expect(function () { this.controller.optionRemove('abc'); }).toThrow();
    });

    it('calling optionRemove removes an option', function() {
      createController.call(this);

      // note: more than two strings in options array
      var options = ['abc', 'def'];
      this.controller.annotationType.valueType = 'Select';
      this.controller.annotationType.maxValueCount = 1;
      this.controller.annotationType.valueTypeChanged();
      this.controller.annotationType.options = options.slice(0);
      this.controller.optionRemove('abc');
      expect(this.controller.annotationType.options).toBeArrayOfSize(options.length - 1);
    });

    it('calling removeButtonDisabled returns valid results', function() {
      createController.call(this);

      // note: more than two strings in options array
      var options = ['abc', 'def'];
      this.controller.annotationType.valueType = 'Select';
      this.controller.annotationType.maxValueCount = 1;
      this.controller.annotationType.valueTypeChanged();
      this.controller.annotationType.options = options.slice(0);

      expect(this.controller.removeButtonDisabled()).toEqual(false);

      this.controller.annotationType.options = options.slice(1);
      expect(this.controller.removeButtonDisabled()).toEqual(true);
    });

    it('should invoke submit function', function() {
      createController.call(this);

      var annotType = new this.AnnotationType(this.factory.annotationType());
      this.controller.submit(annotType);
      expect(this.onSubmit).toHaveBeenCalledWith(annotType);
    });

    it('should invoke cancel function', function() {
      createController.call(this);
      this.controller.cancel();
      expect(this.onCancel).toHaveBeenCalled();
    });

});

});
