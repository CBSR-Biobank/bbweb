/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 * Service to access Specimen Link Annotation Types.
 */
/* @ngInject */
function spcLinkAnnotationTypesServiceFactory(StudyAnnotationTypesService) {

  function SpcLinkAnnotationTypesService() {
    StudyAnnotationTypesService.call(this, 'slannottypes');
  }

  SpcLinkAnnotationTypesService.prototype = Object.create(StudyAnnotationTypesService.prototype);
  SpcLinkAnnotationTypesService.prototype.constructor = SpcLinkAnnotationTypesService;

  return SpcLinkAnnotationTypesService;
}

export default ngModule => ngModule.service('spcLinkAnnotationTypesService',
                                           spcLinkAnnotationTypesServiceFactory)
