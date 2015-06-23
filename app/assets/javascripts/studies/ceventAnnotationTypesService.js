/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  ceventAnnotationTypesFactory.$inject = [
    'StudyAnnotationTypesService'
  ];

  /**
   * Service to access Collection Event Annotation Types.
   */
  function ceventAnnotationTypesFactory(StudyAnnotationTypesService) {

    function CeventAnnotationTypesService() {
      StudyAnnotationTypesService.call(this, 'ceannottypes');
    }

    CeventAnnotationTypesService.prototype = Object.create(StudyAnnotationTypesService.prototype);

    return new CeventAnnotationTypesService();
  }

  return ceventAnnotationTypesFactory;
});
