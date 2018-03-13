/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/* @ngInject */
function SingleSelectAnnotationFactory(Annotation, DomainError) {

  /**
   * Please use annotationFactory.create to create annotation objects.
   */
  function SingleSelectAnnotation(obj = {}, annotationType) {
    Annotation.call(this, obj, annotationType);
    this.valueType = 'SingleSelect';

    if (!_.isUndefined(obj.selectedValues)) {
      if (obj.selectedValues.length === 0) {
        this.value = null;
      } else if (obj.selectedValues.length === 1) {
        this.value = obj.selectedValues[0];
      } else {
        throw new DomainError('invalid value for selected values');
      }
    }
  }

  SingleSelectAnnotation.prototype = Object.create(Annotation.prototype);

  SingleSelectAnnotation.prototype.getValue = function () {
    return this.value;
  };

  SingleSelectAnnotation.prototype.getDisplayValue = function () {
    return this.value;
  };

  SingleSelectAnnotation.prototype.getServerAnnotation = function () {
    var self = this,
        selectedValues = [];

    if (this.value) {
      selectedValues.push(self.value);
    }

    return {
      annotationTypeId: this.getAnnotationTypeId(),
      selectedValues:   selectedValues
    };
  };

  return SingleSelectAnnotation;
}

export default ngModule => ngModule.factory('SingleSelectAnnotation', SingleSelectAnnotationFactory)
