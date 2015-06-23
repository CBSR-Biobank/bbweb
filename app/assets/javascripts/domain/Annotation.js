/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global define */
define(['moment', 'underscore'], function(moment, _) {
  'use strict';

  AnnotationFactory.$inject = [
    'funutils',
    'validationService',
    'AnnotationValueType',
    'bbwebConfig'
  ];

  function AnnotationFactory(funutils,
                             validationService,
                             AnnotationValueType,
                             bbwebConfig) {

    var requiredKeys = ['annotationTypeId', 'selectedValues'];

    var validateIsMap = validationService.condition1(
      validationService.validator('must be a map', _.isObject));

    var createObj = funutils.partial1(validateIsMap, _.identity);

    var validateObj = funutils.partial(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys.apply(null, requiredKeys))),
      createObj);

    var validateSelectedValues = funutils.partial(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys('annotationTypeId', 'value'))),
      createObj);

    /**
     * This object is used by HTML form code to manage annotation information. It differs from the
     * server representation in order to make setting the information via an HTML simpler.
     *
     * @param {Object} obj - the server side entity
     * @param {AnnotationType} annotationType the annotation type this annotation is based from
     * @param {boolean} required set only if annotationType does not have a 'required' attribute.
     */
    function Annotation(obj, annotationType, required) {
      var self = this,
          date,
          defaults = {
            annotationTypeId     : null,
            stringValue          : null,
            numberValue          : null,
            dateTimeValue        : { date: null , time: null },
            singleSelectValue    : null,
            multipleSelectValues : []
          };

      obj = obj || {};
      _.extend(self,
               defaults,
               _.pick(obj, 'annotationTypeId', 'stringValue', 'numberValue', 'selectedValues'));

      if (annotationType) {
        if (annotationType.isValueTypeSelect()) {
          if (annotationType.isSingleSelect()) {
            if (!_.isUndefined(obj.selectedValues) && (obj.selectedValues.length > 0)) {
              self.singleSelectValue = obj.selectedValues[0].value;
            } else {
              self.singleSelectValue = null;
            }
          } else if (annotationType.isMultipleSelect()) {
            self.multipleSelectValues = initializeMultipleSelect();
          }
          this.selectedValues = null;
        } else if (annotationType.isValueTypeDateTime()) {
          if (obj.stringValue) {
            date = moment(obj.stringValue, bbwebConfig.dateTimeFormat).toDate();
            self.dateTimeValue = { date: date, time: date };
            this.stringValue = null;
          }
        } else if (annotationType.isValueTypeNumber()) {
          if (obj.numberValue) {
            self.numberValue = parseFloat(obj.numberValue);
          }
        }
      }

      self.annotationType = annotationType;
      self.displayValue = undefined;

      if (annotationType) {
        if (!_.contains(AnnotationValueType.values(), annotationType.valueType)) {
          throw new Error('value type is invalid: ' + annotationType.valueType);
        }

        if (_.isUndefined(annotationType.required)) {
          if (_.isUndefined(required)) {
            throw new Error('required not assigned');
          }
          self.required = required;
        } else {
          self.required = annotationType.required;
        }

        if (annotationType.valueType === AnnotationValueType.SELECT()) {
          if (!annotationType.isMultipleSelect() && !annotationType.isSingleSelect()) {
            throw new Error('invalid value for max count');
          }
        }
      }

      function initializeMultipleSelect() {
        var result = _.map(annotationType.options, function (opt) {
          return { name: opt, checked: false };
        });
        _.each(obj.selectedValues, function (sv) {
          var value = _.findWhere(result, { name: sv.value });
          value.checked = true;
        });
        return result;
      }
    }

    /**
     * This static method should be used instead of the constructor when creating an annotation from a server
     * response, since it validates that the required fields are present.
     */
    Annotation.create = function (obj, annotationType, required) {
      var annotValid, validation = validateObj(obj);

      if (!_.isObject(validation)) {
        return new Error('invalid object from server: ' + validation);
      }

      annotValid =_.reduce(obj.selectedValues, function (memo, selectedValue) {
        var validation = validateSelectedValues(selectedValue);
        return memo && _.isObject(validation);
      }, true);

      if (!annotValid) {
        return new Error('invalid selected values in object from server');
      }
      return new Annotation(obj, annotationType, required);
    };

    /**
     *
     */
    Annotation.prototype.getAnnotationTypeId = function () {
      if (_.isUndefined(this.annotationType)) {
        throw new Error('annotation type not assigned');
      }
      return this.annotationType.id;
    };

    /**
     *
     */
    Annotation.prototype.getValueType = function () {
      if (_.isUndefined(this.annotationType)) {
        throw new Error('annotation type not assigned');
      }
      return this.annotationType.valueType;
    };

    /**
     * Returns the label to display for the annotation.
     */
    Annotation.prototype.getLabel = function () {
      if (_.isUndefined(this.annotationType)) {
        throw new Error('annotation type not assigned');
      }
      return this.annotationType.name;
    };

    /**
     * For non requried annotation types, this always returns true. For required annotation types,
     * returns true if the value is not empty.
     */
    Annotation.prototype.isValid = function () {
      var value;

      if (!this.required) {
        return true;
      }

      value = this.getValue();
      if (_.isUndefined(value) || _.isNull(value)) {
        return false;
      }

      if (_.isString(value)) {
        value = value.trim();
        return (value !== '');
      }

      return (value !== null);
    };

    /**
     * Returns a value that can be displayed to the user.
     */
    Annotation.prototype.getValue = function () {
      var datetime;

      if (_.isUndefined(this.annotationType)) {
        throw new Error('annotation type not assigned');
      }

      switch (this.annotationType.valueType) {

      case AnnotationValueType.TEXT():
        return this.stringValue;

      case AnnotationValueType.NUMBER():
        return this.numberValue;

      case AnnotationValueType.DATE_TIME():
        // date part is kept in this.dateTimeValue.date and time in this.dateTimeValue.time,
        //
        // they must be combined
        if (this.dateTimeValue.date && this.dateTimeValue.time) {
          if (this.dateTimeValue.time instanceof Date) {
            this.dateTimeValue.time = moment(this.dateTimeValue.time);
          }
          datetime = moment(this.dateTimeValue.date).set({
            'millisecond': 0,
            'second': 0,
            'minute': this.dateTimeValue.time.minutes(),
            'hour': this.dateTimeValue.time.hours()
          });
          return datetime.local().format(bbwebConfig.dateTimeFormat);
        }
        return '';

      case AnnotationValueType.SELECT():
        if (this.annotationType.isSingleSelect()) {
          return this.singleSelectValue;
        }

        return _.chain(this.multipleSelectValues)
          .filter(function (sv) { return sv.checked; })
          .map(function (sv) { return sv.name; })
          .value().join(', ');

      default:
        // should never happen since this is checked for in the constructor, but just in case
        throw new Error('invalid value type for annotation: ' + this.annotationType.valueType);
      }
    };

    // convert annotation to server format
    Annotation.prototype.getServerAnnotation = function () {
      var self = this,
          result,
          datetime;

      result = {
        annotationTypeId: self.getAnnotationTypeId(),
        selectedValues: []
      };

      switch (this.annotationType.valueType) {

      case AnnotationValueType.TEXT():
        result.stringValue = this.stringValue;
        break;

      case AnnotationValueType.NUMBER():
        if (this.numberValue) {
          result.numberValue = this.numberValue.toString();
        } else {
          result.numberValue = '';
        }
        break;

      case AnnotationValueType.DATE_TIME():
        // date part is kept in this.dateTimeValue.date and time in this.dateTimeValue.time,
        //
        // they must be combined
        if (this.dateTimeValue.date && this.dateTimeValue.time) {
          if (this.dateTimeValue.time instanceof Date) {
            this.dateTimeValue.time = moment(this.dateTimeValue.time);
          }
          datetime = moment(this.dateTimeValue.date).set({
            'millisecond': 0,
            'second':      0,
            'minute':      this.dateTimeValue.time.minutes(),
            'hour':        this.dateTimeValue.time.hours()
          });
          result.stringValue = datetime.local().format(bbwebConfig.dateTimeFormat);
        } else {
          result.stringValue = '';
        }
        break;

      case AnnotationValueType.SELECT():
        result.selectedValues = getSelectionsForServer();
        break;

      default:
        // should never happen since this is checked for in the constructor, but just in case
        throw new Error('invalid value type for annotation: ' + this.annotationType.valueType);
      }

      return result;

      function getSelectionsForServer() {
        if (self.annotationType.isSingleSelect() ) {
          if (self.singleSelectValue) {
            return [{ annotationTypeId: self.annotationType.id, value: self.singleSelectValue }];
          } else {
            return [];
          }
        }

        return _.chain(self.multipleSelectValues)
          .filter(function (sv) { return sv.checked; })
          .map(function (sv) {
            return { annotationTypeId: self.annotationType.id, value: sv.name };
          })
          .value();
      }
    };

    /**
     * Returns true if some of the values have the checked field set to true.
     */
    Annotation.prototype.someSelected = function () {
      if (!this.annotationType.isMultipleSelect()) {
        throw new Error('invalid select type: valueType: ' + this.annotationType.valueType +
                        ' maxValueCount:' + this.annotationType.maxValueCount);
      }
      return (_.findWhere(this.multipleSelectValues, { checked: true }) !== undefined);
    };

    /** return constructor function */
    return Annotation;
  }

  return AnnotationFactory;
});
