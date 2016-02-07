/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  ParticipantAnnotationTypeFactory.$inject = [
    'validationService',
    'participantAnnotationTypeValidation',
    'StudyAnnotationType',
    'StudyAnnotationTypesService',
    'participantAnnotationTypesService'
  ];

  /**
   *
   */
  function ParticipantAnnotationTypeFactory(validationService,
                                            participantAnnotationTypeValidation,
                                            StudyAnnotationType,
                                            StudyAnnotationTypesService,
                                            participantAnnotationTypesService) {

    function ParticipantAnnotationType(obj) {
      obj = obj || {};
      StudyAnnotationType.call(this, obj);

      this.required = obj.required || false;
      this._service = participantAnnotationTypesService;
    }

    ParticipantAnnotationType.prototype = Object.create(StudyAnnotationType.prototype);

    ParticipantAnnotationType.prototype.constructor = ParticipantAnnotationType;

    ParticipantAnnotationType.create = function(obj) {
      var annotationType = StudyAnnotationType.create(obj);
      if (!_.isObject(annotationType)) {
        return annotationType;
      }
      return new ParticipantAnnotationType(obj);
    };

    ParticipantAnnotationType.list = function(studyId) {
      return StudyAnnotationType.list(participantAnnotationTypeValidation.validateObj,
                                      ParticipantAnnotationType.create,
                                      'pannottypes',
                                      studyId);
    };

    ParticipantAnnotationType.get = function(studyId, annotationTypeId) {
      return StudyAnnotationType.get(participantAnnotationTypeValidation.validateObj,
                                     ParticipantAnnotationType.create,
                                     'pannottypes',
                                     studyId,
                                     annotationTypeId);
    };

    ParticipantAnnotationType.prototype.addOrUpdate = function () {
      return StudyAnnotationType.prototype
        .addOrUpdate.call(this,
                          participantAnnotationTypeValidation.validateObj,
                          ParticipantAnnotationType.create);
    };

    return ParticipantAnnotationType;
  }

  return ParticipantAnnotationTypeFactory;
});
