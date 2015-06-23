/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  participantAnnotationTypesServiceFactory.$inject = [
    'StudyAnnotationTypesService'
  ];

  /**
   * Service to access participant annotation types.
   */
  function participantAnnotationTypesServiceFactory(StudyAnnotationTypesService) {

    function ParticipantAnnotationTypesService() {
      StudyAnnotationTypesService.call(this, 'pannottypes');
    }

    ParticipantAnnotationTypesService.prototype = Object.create(StudyAnnotationTypesService.prototype);

    return new ParticipantAnnotationTypesService();
  }

  return participantAnnotationTypesServiceFactory;
});
