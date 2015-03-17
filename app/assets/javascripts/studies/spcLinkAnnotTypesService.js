define([], function() {
  'use strict';

  spcLinkAnnotTypesServiceFactory.$inject = [
    'StudyAnnotTypesService'
  ];

  /**
   * Service to access Specimen Link Annotation Types.
   */
  function spcLinkAnnotTypesServiceFactory(StudyAnnotTypesService) {

    function SpcLinkAnnotTypesService() {
      StudyAnnotTypesService.call(this, 'slannottypes');
    }

    SpcLinkAnnotTypesService.prototype = Object.create(StudyAnnotTypesService.prototype);

    return new SpcLinkAnnotTypesService();
  }

  return spcLinkAnnotTypesServiceFactory;
});
