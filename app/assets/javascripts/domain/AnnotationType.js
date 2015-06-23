/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  AnnotationTypeFactory.$inject = [
    'validationService',
    'ConcurrencySafeEntity',
    'AnnotationValueType',
    'AnnotationMaxValueCount'
  ];

  /**
   *
   */
  function AnnotationTypeFactory(validationService,
                                 ConcurrencySafeEntity,
                                 AnnotationValueType,
                                 AnnotationMaxValueCount) {

    function AnnotationType(obj) {
      var defaults = {
        name:          '',
        description:   null,
        valueType:     '',
        maxValueCount: null,
        options:       []
      };

      this._requiredKeys = ['id', 'name', 'valueType', 'options'];

      obj = obj || {};
      ConcurrencySafeEntity.call(this, obj);
      _.extend(this, defaults, _.pick(obj, _.keys(defaults)));
    }

    AnnotationType.prototype = Object.create(ConcurrencySafeEntity.prototype);

    AnnotationType.prototype.isValueTypeText = function () {
      return (this.valueType === AnnotationValueType.TEXT());
    };

    AnnotationType.prototype.isValueTypeNumber = function () {
      return (this.valueType === AnnotationValueType.NUMBER());
    };

    AnnotationType.prototype.isValueTypeDateTime = function () {
      return (this.valueType === AnnotationValueType.DATE_TIME());
    };

    AnnotationType.prototype.isValueTypeSelect = function () {
      return (this.valueType === AnnotationValueType.SELECT());
    };

    AnnotationType.prototype.isSingleSelect = function () {
      return (this.valueType === AnnotationValueType.SELECT()) &&
        (this.maxValueCount === AnnotationMaxValueCount.SELECT_SINGLE());
    };

    AnnotationType.prototype.isMultipleSelect = function () {
      return (this.valueType === AnnotationValueType.SELECT()) &&
        (this.maxValueCount === AnnotationMaxValueCount.SELECT_MULTIPLE());
    };

    /**
     * Returns true if the maxValueCount value is valid.
     */
    AnnotationType.prototype.isMaxValueCountValid = function () {
      if (this.isValueTypeSelect()) {
        return (this.isSingleSelect() || this.isMultipleSelect());
      }
      return ((this.maxValueCount === null) ||
              (this.maxValueCount === AnnotationMaxValueCount.NONE()));
    };

    /**
     * Used to add an option. Should only be called when the value type is 'Select'.
     */
    AnnotationType.prototype.addOption = function () {
      if (!this.isValueTypeSelect()) {
        throw new Error('value type is not select: ' + this.valueType);
      }
      this.options.push('');
    };

    /**
     * Used to remove an option. Should only be called when the value type is 'Select'.
     */
    AnnotationType.prototype.removeOption = function (option) {
      if (this.options.length <= 1) {
        throw new Error('options is empty, cannot remove any more options');
      }
      this.options = _.without(this.options, option);
    };

    return AnnotationType;
  }

  return AnnotationTypeFactory;
});
