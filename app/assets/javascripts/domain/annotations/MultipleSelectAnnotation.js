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
      return _.chain(this.values)
        .filter(function (sv) { return sv.checked; })
        .map(function (sv) { return sv.name; })
        .value().join(', ');
    };

    MultipleSelectAnnotation.prototype.getServerAnnotation = function () {
      var self = this, selectedValues;

      selectedValues =  _.chain(self.values)
        .filter(function (sv) { return sv.checked; })
        .map(function (sv) {
          return sv.name;
        })
        .value();

      return {
        annotationTypeId: this.getAnnotationTypeId(),
        selectedValues:   selectedValues
      };
    };

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
