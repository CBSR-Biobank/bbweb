define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  AnnotationTypeFactory.$inject = [
    'validationService',
    'ConcurrencySafeEntity',
    'AnnotationValueType'
  ];

  /**
   *
   */
  function AnnotationTypeFactory(validationService,
                                 ConcurrencySafeEntity,
                                 AnnotationValueType) {

    function AnnotationType(obj) {
      obj = obj || {};

      ConcurrencySafeEntity.call(this, obj);

      _.extend(this, _.defaults(obj, {
        name:          '',
        description:   null,
        valueType:     '',
        maxValueCount: null,
        options:       []
      }));

      this._requiredKeys = ['id', 'name', 'valueType', 'options'];
    }

    AnnotationType.prototype = Object.create(ConcurrencySafeEntity.prototype);

    AnnotationType.prototype.isValueTypeText = function () {
      return (this.isValueType === AnnotationValueType.TEXT());
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
        (this.maxValueCount === 1);
    };

    AnnotationType.prototype.isMultipleSelect = function () {
      return (this.valueType === AnnotationValueType.SELECT()) &&
        (this.maxValueCount === 2);
    };

    return AnnotationType;
  }

  return AnnotationTypeFactory;
});
