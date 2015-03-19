define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  AnnotationTypeFactory.$inject = [
    'validationService',
    'ConcurrencySafeEntity'
  ];

  /**
   *
   */
  function AnnotationTypeFactory(validationService,
                                 ConcurrencySafeEntity) {

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

    return AnnotationType;
  }

  return AnnotationTypeFactory;
});
