define(['../module', 'underscore'], function(module, _) {
  'use strict';

  module.factory('ParticipantAnnotationType', ParticipantAnnotationTypeFactory);

  ParticipantAnnotationTypeFactory.$inject = [
    'validationService',
    'StudyAnnotationType',
    'participantAnnotTypesService'
  ];

  /**
   *
   */
  function ParticipantAnnotationTypeFactory(validationService,
                                            StudyAnnotationType,
                                            participantAnnotTypesService) {

    function ParticipantAnnotationType(obj) {
      obj = obj || {};
      StudyAnnotationType.call(this, obj);

      this.required = obj.required || false;

      this._requiredKeys.unshift('required');
      this._addedEventRequiredKeys.unshift('required');
      this._updatedEventRequiredKeys.unshift('required');

      this._service = participantAnnotTypesService;
    }

    ParticipantAnnotationType.prototype = Object.create(StudyAnnotationType.prototype);

    return ParticipantAnnotationType;
  }

});
