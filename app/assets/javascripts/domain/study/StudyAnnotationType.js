define(['../module', 'angular', 'underscore'], function(module, angular, _) {
  'use strict';

  module.factory('StudyAnnotationType', StudyAnnotationTypeFactory);

  StudyAnnotationTypeFactory.$inject = ['AnnotationType'];

  /**
   *
   */
  function StudyAnnotationTypeFactory(AnnotationType) {

    function StudyAnnotationType(obj) {
      obj = obj || {};

      AnnotationType.call(this, obj);
      this.studyId = obj.studyId || null;
    }

    StudyAnnotationType.prototype = Object.create(AnnotationType.prototype);

    StudyAnnotationType.prototype.getAddCommand = function () {
      return _.extend(AnnotationType.prototype.getAddCommand.call(this), { studyId: this.studyId });
    };

    return StudyAnnotationType;
  }

});
