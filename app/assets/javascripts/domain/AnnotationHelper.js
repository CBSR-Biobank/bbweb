/* global define */
define(['moment', 'underscore'], function(moment, _) {
  'use strict';

  AnnotationHelperFactory.$inject = ['AnnotationValueType'];

  function AnnotationHelperFactory(AnnotationValueType) {

    /**
     * An annotation helper created from an annotation type. This object is used by HTML form code
     * to manage annotation information.
     *
     * @param annotationType the annotationHelper type this annotationHelper is based from
     * @param required set only if annotationType does not have a 'required' attribute.
     */
    function AnnotationHelper(annotationType, required) {
      var self = this;

      self.annotationType = annotationType;
      self.displayValue = undefined;

      if (_.isUndefined(annotationType.required)) {
        if (_.isUndefined(required)) {
          throw new Error('required not assigned');
        }
        self.required = required;
      } else {
        self.required = annotationType.required;
      }

      switch (annotationType.valueType) {

      case AnnotationValueType.TEXT():
        self.value = undefined;
        break;

      case AnnotationValueType.NUMBER():
        self.value = undefined;
        break;

      case AnnotationValueType.DATE_TIME():
        self.value = {
          date: moment().format('YYYY-MM-DD'),
          time: moment()
        };
        break;

      case AnnotationValueType.SELECT():
        if (annotationType.maxValueCount === 2) {
          self.values = [];
          _.each(annotationType.options, function (option) {
            self.values.push({name: option, checked: false});
          });
        } else if (annotationType.maxValueCount === 1) {
          self.value = undefined;
        } else {
          throw new Error('invalid value for max count');
        }
        break;

      default:
        throw new Error('value type is invalid: ' + annotationType.valueType);
      }
    }

    /**
     *
     */
    AnnotationHelper.prototype.getAnnotationTypeId = function () {
      return this.annotationType.id;
    };

    /**
     * Returns the label to display for the annotation.
     */
    AnnotationHelper.prototype.getLabel = function () {
      return this.annotationType.name;
    };

    /**
     * Assigns the value to an annotation based on the annotation's type.
     *
     * @param annotation.stringValue when setting date must be an ISO date format (i.e. 2015-02-20T09:00:00-0700).
     *
     * Note: for Number value types, the value must be formatted as a string.
     */
    AnnotationHelper.prototype.setValue = function (annotation) {
      var self = this;
      var date;

      switch (self.annotationType.valueType) {

      case AnnotationValueType.TEXT():
        self.value = annotation.stringValue;
        self.displayValue = annotation.stringValue;
        break;

      case AnnotationValueType.NUMBER():
        self.value = parseFloat(annotation.numberValue);
        self.displayValue = self.value;
        break;

      case AnnotationValueType.DATE_TIME():
        // date part is kept in self.value.date and time in self.value.time, they must be combined
        date = moment(annotation.stringValue);
        self.value.date = date.format('YYYY-MM-DD');
        self.value.time = date;

        self.displayValue = date.local().format('YYYY-MM-DD h:mm A') + ' (' + date.local().fromNow() + ')';
        break;

      case AnnotationValueType.SELECT():
        if (self.annotationType.maxValueCount === 1) {
          self.value = annotation.selectedValues[0].value;
          self.displayValue = self.value;
        } else if (self.annotationType.maxValueCount > 1) {
          // set the 'checked' property on all the selected values
          _.each(annotation.selectedValues, function(selectedValue) {
            var value = _.findWhere(self.values,  { name: selectedValue.value });
            if (value) {
              value.checked = true;
            }
          });

          self.displayValue = _.chain(self.values)
            .filter(function(value) { return value.checked; })
            .map(function(value) { return value.name; })
            .value()
            .join(', ');
        } else {
          throw new Error('invalid max value count for annotation: ' + self.annotationType.maxValueCount);
        }
        break;

      default:
        throw new Error('invalid value type for annotation: ' + self.annotationType.valueType);
      }
    };

    /**
     * Returns an Annotation that can be used with the server's REST API
     */
    AnnotationHelper.prototype.getAnnotation = function () {
      var self = this;
      var result = {
        annotationTypeId: self.annotationType.id,
        selectedValues:   getSelectedValues()
      };

      var value = getAnnotationValue();
      if (value) {
        _.extend(result, value);
      }

      return result;

      //---

      function getSelectedValues() {
        if (!self.annotationType.isValueTypeSelect()) {
          return [];
        }

        if (self.annotationType.isSingleSelect()) {
          return [{
            annotationTypeId: self.annotationType.id,
            value:            self.value
          }];
        }

        return _.chain(self.values)
          .filter(function(value) { return value.checked; })
          .map(function(value) {
            return {
              annotationTypeId: self.annotationType.id,
              value:            value.name
            };
          })
          .value();
      }

      /**
       * Returns the annotaions current value as a string..
       */
      function getAnnotationValue() {
        var datetime;

        switch (self.annotationType.valueType) {

        case AnnotationValueType.TEXT():
          return { stringValue: self.value };

        case AnnotationValueType.NUMBER():
          return { numberValue: self.value.toString() };

        case AnnotationValueType.DATE_TIME():
          // date part is kept in self.value.date and time in self.value.time, they must be combined
          if (self.value.time instanceof Date) {
            self.value.time = moment(self.value.time);
          }
          datetime = moment(self.value.date).set({
            'millisecond': 0,
            'second': 0,
            'minute': self.value.time.minutes(),
            'hour': self.value.time.hours()
          });
          return { stringValue: datetime.local().format() };
        }

        return null;
      }
    };

    AnnotationHelper.prototype.getDisplayValue = function () {
      var annotation = this.getAnnotation();
      if (annotation.stringValue) {
        return annotation.stringValue;
      } else if (annotation.numberValue) {
        return annotation.numberValue;
      } else {
        return _.pluck(annotation.selectedValues, 'value').join(', ');
      }
    };

    /**
     * Returns true if some of the values have the checked field set to true.
     */
    AnnotationHelper.prototype.someSelected = function () {
      if (!this.annotationType.isMultipleSelect()) {
        throw new Error('invalid select type: valueType:' + this.annotationType.valueType +
                        ' maxValueCount:' + this.annotationType.maxValueCount);
      }
      return (_.findWhere(this.values, { checked: true }) !== undefined);
    };

    /** return constructor function */
    return AnnotationHelper;
  }

  return AnnotationHelperFactory;
});
