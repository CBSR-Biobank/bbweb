define(['./module', 'angular', 'underscore'], function(module, angular, _) {
  'use strict';

  module.factory('AnnotationType', AnnotationTypeFactory);

  AnnotationTypeFactory.$inject = ['ConcurrencySafeEntity', 'AnnotationValueType'];

  /**
   *
   */
  function AnnotationTypeFactory(ConcurrencySafeEntity, AnnotationValueType) {

    function AnnotationType(obj) {
      obj = obj || {};

      ConcurrencySafeEntity.call(this, obj);

      this.name          = obj.name || '';
      this.description   = obj.desciption || null;
      this.valueType     = obj.valueType || AnnotationValueType.TEXT();
      this.maxValueCount = obj.maxValueCount || null;
      this.options       = obj.options || [];
    }

    AnnotationType.prototype = Object.create(ConcurrencySafeEntity.prototype);

    AnnotationType.prototype.getAddCommand = function () {
      var cmd = _.pick(this, ['studyId', 'name', 'valueType', 'options']);
      if (this.description) {
        cmd.description = this.description;
      }
      if (this.valueType === AnnotationValueType.SELECT()) {
        cmd.maxValueCount = this.maxValueCount;
      }
      return cmd;
    };

    AnnotationType.prototype.getUpdateCommand = function () {
      return _.extend(this.getAddCommand(), {
        id:              this.id,
        expectedVersion: this.version
      });
    };

    return AnnotationType;
  }

});
