/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 *
 * Jasmine test suite
 */
define(function(require) {
  'use strict';

  var mocks   = require('angularMocks'),
      _       = require('lodash');

  function SuiteMixinFactory(EntityuTestSuiteMixin) {

    function SuiteMixin() {
      EntityuTestSuiteMixin.call(this);
    }

    SuiteMixin.prototype = Object.create(EntityuTestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    SuiteMixin.prototype.createAnnotationType = function (options) {
      options = options || {};
      return new this.AnnotationType(this.factory.annotationType(options));
    };

    SuiteMixin.prototype.getAnnotationAndType = function (annotTypeOptions) {
      var annotationType,
          value,
          jsonAnnotation,
          annotation;

      annotTypeOptions = annotTypeOptions || {};

      annotationType   = this.createAnnotationType(annotTypeOptions);
      value            = this.factory.valueForAnnotation(annotationType);
      jsonAnnotation   = this.factory.annotation({ value: value }, annotationType);
      annotation       = this.annotationFactory.create(jsonAnnotation, annotationType);

      return {
        annotationType:   annotationType,
        serverAnnotation: jsonAnnotation,
        annotation:       annotation
      };
    };

    /*
     * Creates annotation type options to create an annotation of each type of object.
     */
    SuiteMixin.prototype.getAnnotationTypeOptionsForAll = function () {
      var result = _.map(_.values(this.AnnotationValueType), function (valueType) {
        return { valueType: valueType };
      });
      result.push({
        valueType: this.AnnotationValueType.SELECT,
        maxValueCount: this.AnnotationMaxValueCount.SELECT_MULTIPLE,
        options: [ 'opt1', 'opt2', 'opt3' ]
      });
      return result;
    };

    /*
     * Creates a set of annotation type, server annotation and annotation object for each type
     * of annotation.
     */
    SuiteMixin.prototype.getAnnotationAndTypeForAllValueTypes = function() {
      var self = this;
      return _.map(self.getAnnotationTypeOptionsForAll(), function (annotationTypeOptions) {
        return self.getAnnotationAndType(annotationTypeOptions);
      });
    };

    return SuiteMixin;
  }

  describe('Annotation', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(EntityTestSuiteMixin, testUtils, testDomainEntities) {
      var SuiteMixin = new SuiteMixinFactory(EntityTestSuiteMixin);
      _.extend(this, SuiteMixin.prototype);
      this.injectDependencies('AppConfig',
                              'Study',
                              'AnnotationType',
                              'annotationFactory',
                              'Annotation',
                              'AnnotationValueType',
                              'AnnotationMaxValueCount',
                              'timeService',
                              'factory');

      testUtils.addCustomMatchers();
      testDomainEntities.extend();
    }));

    it('constructor throws error for an invalid annotation value type', function() {
      var self = this,
          annotationType = self.createAnnotationType({ valueType: self.factory.stringNext() });

      expect(function () {
        var annotation = new self.Annotation({}, annotationType);
        expect(annotation).not.toBeNull();
      }).toThrowError(/value type is invalid:/);
    });

    it('constructor throws error if annotation type is missing the required attribute', function() {
      var self = this,
          annotationType = _.omit(self.createAnnotationType(), 'required');

      expect(function () {
        var annotation = new self.Annotation({}, annotationType);
        expect(annotation).not.toBeNull();
      }).toThrowError(/required not defined/);
    });

    it('constructor throws error for select annotation type and is not multiple or single select', function() {
      var self = this,
          annotationType = _.omit(self.createAnnotationType({ valueType: self.AnnotationValueType.SELECT }),
                                  'maxValueCount');

      expect(function () {
        var annotation = new self.Annotation({}, annotationType);
        expect(annotation).not.toBeNull();
      }).toThrowError(/invalid value for max count/);
    });

    describe('when creating', function() {

      it('fails when creating from an object missing annotation type ID', function() {
        var self = this,
            annotationTypeJson = this.factory.annotationType(),
            badAnnotationJson = _.omit(self.factory.annotation({}, annotationTypeJson), 'annotationTypeId');

        expect(function () {
          self.Annotation.create(badAnnotationJson);
        }).toThrowError(/invalid object to create from/);
      });

      it('fails when creating from an object missing selected values', function() {
        var self = this,
            annotationTypeJson = this.factory.annotationType(),
            badAnnotationJson = _.omit(self.factory.annotation({}, annotationTypeJson), 'selectedValues');

        expect(function () {
          self.Annotation.create(badAnnotationJson);
        }).toThrowError(/invalid object to create from/);
      });


      it('can create annotation with empty value', function() {
        var self = this;

        _.each(self.getAnnotationTypeOptionsForAll(), function (annotationTypeOptions) {
          var annotationType,
              jsonAnnotation,
              annotation;

          annotationTypeOptions.required = true;

          annotationType = self.createAnnotationType(annotationTypeOptions);
          jsonAnnotation = self.factory.annotation(null, annotationType);
          annotation = self.annotationFactory.create(jsonAnnotation, annotationType);
          expect(annotation.getValue()).toBeFalsy();
        });
      });

      it('fails when creating without an annotation type', function() {
        var self = this;
        expect(function () { return self.annotationFactory.create(undefined); })
          .toThrowError('annotation type is undefined');
      });

      it('fails when creating with required parameter omitted', function() {
        var annotationType;

        annotationType = this.createAnnotationType({ valueType: this.AnnotationValueType.TEXT });

        expect(function () { return this.annotationFactory.create(undefined, annotationType); })
          .not.toThrow(new Error('required not assigned'));
      });

      it('creation fails if value type is invalid', function() {
        var self = this,
            annotationType = this.createAnnotationType({ valueType: 'ABCDEF' });
        expect(function () { return self.annotationFactory.create(undefined, annotationType); })
          .toThrowError('value type is invalid: ABCDEF');
      });

      it('creation fails if max value count is invalid', function() {
        var self = this,
            annotationType = this.createAnnotationType({ valueType: this.AnnotationValueType.SELECT });
        annotationType.maxValueCount = 0;
        expect(function () {
          return self.annotationFactory.create(undefined, annotationType);
        }).toThrowError(/invalid select annotation/);

        annotationType.maxValueCount = 3;
        expect(function () {
          return self.annotationFactory.create(undefined, annotationType);
        }).toThrowError(/invalid select annotation/);
      });

      it('creation fails for SINGLE SELECT and multiple values are selected in server object', function() {
        var self = this,
            annotationType,
            serverAnnotation,
            value;

        annotationType = self.createAnnotationType({
          valueType:     self.AnnotationValueType.SELECT,
          maxValueCount: self.AnnotationMaxValueCount.SELECT_SINGLE,
          options:       [ 'option1', 'option2', 'option3' ],
          required:      true
        });

        serverAnnotation = self.factory.annotation(value, annotationType);
        serverAnnotation.selectedValues = annotationType.options;
        expect(function () { return self.annotationFactory.create(serverAnnotation, annotationType); })
          .toThrowError('invalid value for selected values');
      });

      it('fails when creating from a non object', function() {
        var self = this,
            annotationType = self.createAnnotationType({
              valueType:     self.AnnotationValueType.SELECT,
              maxValueCount: self.AnnotationMaxValueCount.SELECT_SINGLE,
              options:       [ 'option1', 'option2', 'option3' ],
              required:      true
            });
        expect(function () { self.annotationFactory.create(1, annotationType); })
          .toThrowError(/invalid object from server/);
      });

      it('fails when creating from server response with bad selections', function() {
        var self = this,
            annotationType = this.createAnnotationType({
              valueType:     self.AnnotationValueType.SELECT,
              maxValueCount: self.AnnotationMaxValueCount.SELECT_SINGLE,
              options:       [ 'option1', 'option2', 'option3' ],
              required:      true
            }),
            jsonAnnotation = {
              annotationTypeId: self.factory.stringNext(),
              selectedValues: { tmp: 1 }
            };
        expect(function () { self.annotationFactory.create(jsonAnnotation, annotationType); })
          .toThrowError(/invalid object from server/);
      });

      it('has valid values when created from server response', function() {
        _.each(this.getAnnotationAndTypeForAllValueTypes(), function (entities) {
          entities.annotation.compareToJsonEntity(entities.serverAnnotation);
        });
      });

    });

    it('getAnnotationTypeId throws an error if annotation type was never assigned', function() {
      var self = this,
          annotation = new self.Annotation({});
      expect(function () {
        annotation.getAnnotationTypeId();
      }).toThrowError(/annotation type not assigned/);
    });

    it('calling getAnnotationTypeId gives a valid result', function() {
      _.each(this.getAnnotationAndTypeForAllValueTypes(), function (entities) {
        expect(entities.annotation.getAnnotationTypeId())
          .toBe(entities.annotationType.uniqueId);
      });
    });

    describe('calling getValueType', function() {

      it('calling getValueType returns the annotation types value type', function() {
        _.each(this.getAnnotationAndTypeForAllValueTypes(), function (entities) {
          expect(entities.annotation.getValueType())
            .toBe(entities.annotationType.valueType);
        });
      });

      it('throws an error if annotation type is not assigned', function() {
        var self = this,
            annotation = new self.Annotation({});
        expect(function () {
          annotation.getValueType();
        }).toThrowError(/annotation type not assigned/);
      });

    });

    describe('calling getLabel', function() {

      it('calling getLabel returns the annotation types name', function() {
        _.each(this.getAnnotationAndTypeForAllValueTypes(), function (entities) {
          expect(entities.annotation.getLabel())
            .toBe(entities.annotationType.name);
        });
      });

      it('throws an error if annotation type is not assigned', function() {
        var self = this,
            annotation = new self.Annotation({});
        expect(function () {
          annotation.getLabel();
        }).toThrowError(/annotation type not assigned/);
      });

    });

    describe('calling isValueValid', function() {

      it('returns true if the annotation is not required', function() {
        _.each(this.getAnnotationAndTypeForAllValueTypes(), function (entities) {
          expect(entities.annotation.isValueValid()).toBe(true);
        });
      });

      it('returns FALSE if the annotation is required and has no value', function() {
        var self = this;
        _.each(self.getAnnotationTypeOptionsForAll(), function (annotationTypeOptions) {
          var annotationType,
              serverAnnotation,
              annotation;

          annotationTypeOptions.required = true;

          annotationType = self.createAnnotationType(annotationTypeOptions);
          serverAnnotation = self.factory.annotation(null, annotationType);
          annotation = self.annotationFactory.create(serverAnnotation, annotationType, true);
          expect(annotation.isValueValid()).toBe(false);
        });

      });

      it('returns TRUE if the annotation is required and has a value', function() {
        var self = this;
        _.each(self.getAnnotationTypeOptionsForAll(), function (annotationTypeOptions) {
          var annotationType,
              value,
              serverAnnotation,
              annotation;

          annotationTypeOptions.required = true;

          annotationType   = self.createAnnotationType(annotationTypeOptions);
          value            = self.factory.valueForAnnotation(annotationType);
          serverAnnotation = self.factory.annotation({ value: value }, annotationType);
          annotation       = self.annotationFactory.create(serverAnnotation, annotationType, true);
          expect(annotation.isValueValid()).toBe(true);
        });
      });

    });

    it('getValue returns valid results for TEXT and DATE_TIME annotation types', function() {
      var self = this,
          annotationType,
          annotation,
          serverAnnotation,
          value,
          valueTypes = [ self.AnnotationValueType.TEXT, self.AnnotationValueType.DATE_TIME ],
          timeStr;

      _.each(valueTypes, function (valueType) {
        annotationType = self.createAnnotationType({ valueType: valueType });

        value = self.factory.valueForAnnotation(annotationType);
        serverAnnotation = self.factory.annotation({ value: value }, annotationType);
        annotation = self.annotationFactory.create(serverAnnotation, annotationType);

        if (valueType === self.AnnotationValueType.TEXT) {
          expect(annotation.getValue()).toEqual(serverAnnotation.stringValue);
        } else {
          timeStr = self.timeService.dateToDisplayString(serverAnnotation.stringValue);
          expect(annotation.getValue()).toEqual(timeStr);
        }

      });
    });

    it('getValue returns valid results for NUMBER annotation type', function() {
      var annotationType,
          annotation,
          serverAnnotation,
          value;

      annotationType = this.createAnnotationType({ valueType: this.AnnotationValueType.NUMBER });

      value = this.factory.valueForAnnotation(annotationType);
      serverAnnotation = this.factory.annotation({ value: value }, annotationType);

      annotation = this.annotationFactory.create(serverAnnotation, annotationType);
      expect(annotation.getValue()).toEqual(parseFloat(serverAnnotation.numberValue));
    });

    it('getValue returns valid results for SINGLE SELECT', function() {
      var annotationType, annotation, serverAnnotation, value;

      annotationType = this.createAnnotationType({
        valueType:     this.AnnotationValueType.SELECT,
        maxValueCount: this.AnnotationMaxValueCount.SELECT_SINGLE,
        options:       [ 'option1', 'option2' ],
        required:      true
      });

      value = this.factory.valueForAnnotation(annotationType);
      serverAnnotation = this.factory.annotation({ value: value }, annotationType);

      annotation = this.annotationFactory.create(serverAnnotation, annotationType);
      expect(annotation.getValue()).toEqual(serverAnnotation.selectedValues[0]);
    });

    it('getValue returns valid results for MULTIPLE SELECT', function() {
      var annotationType, annotation, serverAnnotation, value;

      annotationType = this.createAnnotationType({
        valueType:     this.AnnotationValueType.SELECT,
        maxValueCount: this.AnnotationMaxValueCount.SELECT_MULTIPLE,
        options:       [ 'option1', 'option2', 'option3' ],
        required:      true
      });

      value = this.factory.valueForAnnotation(annotationType);
      serverAnnotation = this.factory.annotation({ value: value }, annotationType);
      annotation = this.annotationFactory.create(serverAnnotation, annotationType);

      expect(annotation.getValue()).toEqual(serverAnnotation.selectedValues.join(', '));
    });

    it('getServerAnnotation returns valid results for non select annotation types', function() {
      _.each(this.getAnnotationAndTypeForAllValueTypes(), function (entities) {
        var serverAnnot = entities.annotation.getServerAnnotation();
        _.each(_.keys(serverAnnot), function (key) {
          expect(serverAnnot[key]).toEqual(entities.serverAnnotation[key]);
        });
      });
    });

    it('getServerAnnotation returns valid results for annotation with empty value', function() {
      var self = this;
      _.each(self.getAnnotationTypeOptionsForAll(), function (annotationTypeOptions) {
        var annotationType,
            serverAnnotation,
            annotation;

        annotationTypeOptions.required = true;

        annotationType = self.createAnnotationType(annotationTypeOptions);
        serverAnnotation = self.factory.annotation({ value: '' }, annotationType);
        annotation = self.annotationFactory.create(serverAnnotation, annotationType, true);

        var serverAnnot = annotation.getServerAnnotation();
        _.each(_.keys(serverAnnot), function (key) {
          expect(serverAnnot[key]).toEqual(serverAnnotation[key]);
        });
      });
    });

    it('someSelected returns valid results for multiple select', function() {
      var annotationType, annotation;

      annotationType = this.createAnnotationType({
        valueType:     this.AnnotationValueType.SELECT,
        maxValueCount: this.AnnotationMaxValueCount.SELECT_MULTIPLE,
        options:       [ 'option1', 'option2' ],
        required:      true
      });

      annotation = this.annotationFactory.create(undefined, annotationType);

      expect(annotationType.options).not.toBeEmptyArray();
      expect(annotation.values).toBeArrayOfSize(annotationType.options.length);
      expect(annotation.someSelected()).toBe(false);

      annotation.values[0].checked = true;
      expect(annotation.someSelected()).toBe(true);
    });

    it('calling setValue assigns the value', function() {
      var annotation = new this.Annotation(),
          newValue = this.factory.stringNext();
      annotation.setValue(newValue);
      expect(annotation.value).toBe(newValue);
    });


  });

});
