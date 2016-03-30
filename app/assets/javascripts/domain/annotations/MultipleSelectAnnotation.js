/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  MultipleSelectAnnotationFactory.$inject = ['Annotation'];

  function MultipleSelectAnnotationFactory(Annotation) {

    /**
     * Please use annotationFactory.create to create annotation objects.
     */
    function MultipleSelectAnnotation(obj, annotationType) {
      var self = this;

      obj = obj || {};
      Annotation.call(this, obj, annotationType);

      self.values = initializeMultipleSelect();
      self.valueType = 'MultipleSelect';

      function initializeMultipleSelect() {
        var result = _.map(annotationType.options, function (opt) {
          return { name: opt, checked: false };
        });
        _.each(obj.selectedValues, function (sv) {
          var value = _.findWhere(result, { name: sv });
          value.checked = true;
        });
        return result;
      }
    }

    MultipleSelectAnnotation.prototype = Object.create(Annotation.prototype);

    MultipleSelectAnnotation.prototype.getValue = function () {
      this.displayValue = getValueAsArray(this.values).join(', ');
      return this.displayValue;
    };

    MultipleSelectAnnotation.prototype.setValue = function (value) {
      if (!_.isArray(value)) {
        throw new Error('value is not an array');
      }
      this.values = value;
    };

    MultipleSelectAnnotation.prototype.getServerAnnotation = function () {
      var self = this;
      return {
        annotationTypeId: self.getAnnotationTypeId(),
        selectedValues:   getValueAsArray(self.values)
      };
    };

    function getValueAsArray(values) {
      var checked = _.matcher({ checked: true });

      return _.chain(values)
        .filter(checked)
        .pluck('name')
        .value();
    }

    /**
     * Returns true if some of the values have the checked field set to true.
     */
    MultipleSelectAnnotation.prototype.someSelected = function () {
      return (_.findWhere(this.values, { checked: true }) !== undefined);
    };

    return MultipleSelectAnnotation;
  }

  return MultipleSelectAnnotationFactory;
});
