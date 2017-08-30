/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  spcLinkAnnotationTypesServiceFactory.$inject = [
    'StudyAnnotationTypesService'
  ];

  /**
   * Service to access Specimen Link Annotation Types.
   */
  function spcLinkAnnotationTypesServiceFactory(StudyAnnotationTypesService) {

    function SpcLinkAnnotationTypesService() {
      StudyAnnotationTypesService.call(this, 'slannottypes');
    }

    SpcLinkAnnotationTypesService.prototype = Object.create(StudyAnnotationTypesService.prototype);

    return new SpcLinkAnnotationTypesService();
  }

  return spcLinkAnnotationTypesServiceFactory;
});
