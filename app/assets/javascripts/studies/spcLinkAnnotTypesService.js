define(['./module'], function(module) {
  'use strict';

  module.service('spcLinkAnnotTypesService', spcLinkAnnotTypesService);

  spcLinkAnnotTypesService.$inject = [
    'StudyAnnotTypesService'
  ];

  /**
   * Service to access Specimen Link Annotation Types.
   */
  function spcLinkAnnotTypesService(StudyAnnotTypesService) {

    function SpcLinkAnnotTypesService() {
      StudyAnnotTypesService.call(this, 'slannottypes');
    }

    SpcLinkAnnotTypesService.prototype = Object.create(StudyAnnotTypesService.prototype);

    return new SpcLinkAnnotTypesService();
  }

});
