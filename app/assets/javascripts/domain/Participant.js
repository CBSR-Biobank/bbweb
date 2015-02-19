/* global define */
define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.factory('Participant', ParticipantFactory);

  ParticipantFactory.$inject = ['AnnotationHelper'];

  /**
   * Factory for participants.
   */
  function ParticipantFactory(AnnotationHelper) {

    function Participant(study, participant, annotationTypes) {
      var self = this;

      _.extend(self, participant);

      self.isNew        = !self.id;
      self.study        = study;
      self.studyId      = study.id;

      self.annotationHelpers = getAnnotationHelpers(annotationTypes);

      function getAnnotationHelpers(annotationTypes) {
        return _.map(annotationTypes, function(annotType) {
          var helper =  new AnnotationHelper(annotType);
          var annotation = _.findWhere(self.annotations, {id: annotation.annotationTypeId});
          if (annotation) {
            helper.setValue(annotation);
          }
          return helper;
        });
      }
    }

    Participant.prototype.setUniqueId = function (uniqueId) {
      this.uniqueId = uniqueId;
    };

    Participant.prototype.getAnnotationHelpers = function () {
      return this.annotationHelpers;
    };

    /**
     * Updated the annotations array with the values assigned to the annotation helpers.
     */
    Participant.prototype.updateAnnotations = function () {
      var self = this;
      self.annotations = [];
      return _.each(self.annotationHelpers, function (annotationHelper) {
        self.annotations.push(annotationHelper.getAnnotation());
      });
    };

    /** return constructor function */
    return Participant;
  }

});
