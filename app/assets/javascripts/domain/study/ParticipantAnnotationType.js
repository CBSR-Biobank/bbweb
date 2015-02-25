define(['../module', 'underscore'], function(module, _) {
  'use strict';

  module.factory('ParticipantAnnotationType', ParticipantAnnotationTypeFactory);

  ParticipantAnnotationTypeFactory.$inject = ['StudyAnnotationType'];

  /**
   *
   */
  function ParticipantAnnotationTypeFactory(StudyAnnotationType) {

    function ParticipantAnnotationType(obj) {
      obj = obj || {};
      StudyAnnotationType.call(this, obj);
      this.required = obj.required || false;
    }

    ParticipantAnnotationType.prototype = Object.create(StudyAnnotationType.prototype);

    ParticipantAnnotationType.prototype.getAddCommand = function () {
      return _.extend(
        StudyAnnotationType.prototype.getAddCommand.call(this),
        { required: this.required });
    };

    return ParticipantAnnotationType;
  }

});
