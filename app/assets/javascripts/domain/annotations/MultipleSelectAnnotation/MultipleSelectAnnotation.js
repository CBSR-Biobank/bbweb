/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/* @ngInject */
function MultipleSelectAnnotationFactory(Annotation, DomainError) {

  /**
   * Please use annotationFactory.create to create annotation objects.
   */
  function MultipleSelectAnnotation(obj = {}, annotationType) {
    Annotation.call(this, obj, annotationType);

    this.values = initializeMultipleSelect();
    this.valueType = 'MultipleSelect';

    function initializeMultipleSelect() {
      var result = annotationType.options.map((opt) => ({ name: opt, checked: false }));
      if (obj.selectedValues) {
        obj.selectedValues.forEach((sv) => {
          var value = result.find(option => option.name === sv);
          value.checked = true;
        });
      }
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
    if (!Array.isArray(value)) {
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
    return !(_.isNil(value) || (Array.isArray(value) && (value.length === 0)));
  };

  function getValueAsArray(values) {
    return values
      .filter(value => value.checked)
      .map(value => value.name);
  }

  /**
   * Returns true if some of the values have the checked field set to true.
   */
  MultipleSelectAnnotation.prototype.someSelected = function () {
    return (this.values.find(value => value.checked) !== undefined);
  };

  return MultipleSelectAnnotation;
}

export default ngModule => ngModule.factory('MultipleSelectAnnotation', MultipleSelectAnnotationFactory)
