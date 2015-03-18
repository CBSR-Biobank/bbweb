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
        valueType:     AnnotationValueType.TEXT(),
        maxValueCount: null,
        options:       []
      }));

      this._requiredKeys = ['id', 'name', 'valueType', 'options'];
    }

    AnnotationType.prototype = Object.create(ConcurrencySafeEntity.prototype);

    return AnnotationType;
  }

  return AnnotationTypeFactory;
});
