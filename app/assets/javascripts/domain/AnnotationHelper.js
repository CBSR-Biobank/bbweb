/* global define */
define(['./module', 'moment', 'underscore'], function(module, moment, _) {
  'use strict';

  module.service('AnnotationHelper', AnnotationHelperFactory);

  //AnnotationFactory.$inject = [];

  /**
   * An annotation helper created from an annotation type. This object is used by HTML form code
   * to manage annotation information.
   */
  function AnnotationHelperFactory() {
    /**
     * @param annotationType the annotationHelper type this annotationHelper is based from
     * @param required set only if annotationType does not have a 'required' attribute.
     */
    function AnnotationHelper(annotationType, required) {
      var self = this;

      self.annotationType = annotationType;
      self.displayValue = undefined;

      if (annotationType.required) {
        self.required = annotationType.required;
      } else {
        if (!!required) {
          self.required = required;
        }
        throw new Error('required not assigned');
      }

      if (annotationType.valueType === 'DateTime') {
        self.value = {
          date: moment().format('YYYY-MM-DD'),
          time: moment()
        };
      } else if ((annotationType.valueType === 'Select') && (annotationType.maxValueCount > 1)) {
        self.values = [];
        _.each(annotationType.options, function (option) {
          self.values.push({name: option, checked: false});
        });
        self.someSelected = function () {
          return _.find(self.values, function(value) {
            return value.checked;
          });
        };
      } else {
        self.value = undefined;
      }
    }

    /**
     *
     */
    AnnotationHelper.prototype.getAnnotationTypeId = function () {
      return this.annotationType.id;
    };

    /**
     *
     */
    AnnotationHelper.prototype.getSelectedValues = function () {
      var self = this;
      if (self.annotationType.valueType !== 'Select') {
        return [];
      }

      if (self.annotationType.maxValueCount === 1) {
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
    };

    /**
     *
     */
    AnnotationHelper.prototype.getLabel = function () {
      return this.annotationType.name;
    };

    /**
     *
     */
    AnnotationHelper.prototype.setValue = function (annotation) {
      var self = this;
      var date;

      if (self.annotationType.valueType === 'Text') {
        self.value = annotation.stringValue;
        self.displayValue = annotation.stringValue;
      } else if (self.annotationType.valueType === 'Number') {
        self.value = parseFloat(annotation.numberValue);
        self.displayValue = self.value;
      } else if (self.annotationType.valueType === 'DateTime') {
        // date part is kept in self.value.date and time in self.value.time, they must be combined
        date = moment(annotation.stringValue);
        self.value.date = date.format('YYYY-MM-DD');
        self.value.time = date;

        self.displayValue = date.local().format('YYYY-MM-DD h:mm A') + ' (' + date.local().fromNow() + ')';
      } else if (self.annotationType.valueType === 'Select') {
        if (self.annotationType.maxValueCount === 1) {
          self.value = annotation.selectedValues[0].value;
          self.displayValue = self.value;
        } else if (self.annotationType.maxValueCount > 1) {
          // set the 'checked' property on all the selected values
          _.each(annotation.selectedValues, function(selectedValue) {
            var value = _.find(self.values, function (selfValue) {
              return selectedValue.value === selfValue.name;
            });
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

      } else {
        throw new Error('invalid value type for annotation: ' + self.annotationType.valueType);
      }
    };

    /**
     *
     */
    AnnotationHelper.prototype.getAnnotationValue = function () {
      var self = this;
      var datetime;

      if (self.annotationType.valueType === 'Text') {
        return { stringValue: self.value };
      }

      if (self.annotationType.valueType === 'Number') {
        return { numberValue: self.value.toString() };
      }

      if (self.annotationType.valueType === 'DateTime') {
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
    };

    /**
     * Returns an Annotation that can be used with the server's REST API
     */
    AnnotationHelper.prototype.getAnnotation = function () {
      var self = this;
      var result = {
        annotationTypeId: self.annotationType.id,
        selectedValues:   self.getSelectedValues()
      };

      var value = self.getAnnotationValue();
      if (value) {
        _.extend(result, value);
      }

      return result;
    };

    /** return constructor function */
    return AnnotationHelper;
  }

});
