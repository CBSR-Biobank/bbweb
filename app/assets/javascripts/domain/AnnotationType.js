define(['./module', 'angular', 'underscore'], function(module, angular, _) {
  'use strict';

  module.factory('AnnotationType', AnnotationTypeFactory);

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
        desciption:    null,
        valueType:     AnnotationValueType.TEXT(),
        maxValueCount: null,
        options:       []
      }));

      this._requiredKeys = ['id', 'name', 'valueType', 'options'];
    }

    AnnotationType.prototype = Object.create(ConcurrencySafeEntity.prototype);

    return AnnotationType;
  }

});
