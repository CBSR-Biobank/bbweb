/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  MultipleSelectAnnotationFactory.$inject = ['Annotation', 'DomainError'];

  function MultipleSelectAnnotationFactory(Annotation, DomainError) {

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
          var value = _.find(result, { name: sv });
          value.checked = true;
        });
        return result;
      }
    }

    MultipleSelectAnnotation.prototype = Object.create(Annotation.prototype);

    MultipleSelectAnnotation.prototype.getValue = function () {
      return getValueAsArray(this.values);
    };

    MultipleSelectAnnotation.prototype.getDisplayValue = function () {
      return this.getValue().join(', ');
    };

    MultipleSelectAnnotation.prototype.setValue = function (value) {
      if (!_.isArray(value)) {
        throw new DomainError('value is not an array');
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

    /**
     * For non requried annotation types, this always returns true. For required annotation types,
     * returns true if the value is not empty.
     */
    MultipleSelectAnnotation.prototype.isValueValid = function () {
      var value;

      if (!this.required) {
        return true;
      }

      value = this.getValue();
      return !(_.isUndefined(value) || _.isNull(value) || (_.isArray(value) && (value.length === 0)));
    };

    function getValueAsArray(values) {
      return _.chain(values)
        .filter({ checked: true })
        .map('name')
        .value();
    }

    /**
     * Returns true if some of the values have the checked field set to true.
     */
    MultipleSelectAnnotation.prototype.someSelected = function () {
      return (_.find(this.values, { checked: true }) !== undefined);
    };

    return MultipleSelectAnnotation;
  }

  return MultipleSelectAnnotationFactory;
});
