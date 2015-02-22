define(['./module'], function(module) {
  'use strict';

  module.service('spcLinkAnnotTypesService', spcLinkAnnotTypesService);

  spcLinkAnnotTypesService.$inject = [
    'StudyAnnotTypesService',
    'SpecimenLinkAnnotationType'
  ];

  /**
   * Service to access Specimen Link Annotation Types.
   */
  function spcLinkAnnotTypesService(StudyAnnotTypesService,
                                    SpecimenLinkAnnotationType) {

    function SpcLinkAnnotTypesService() {
      StudyAnnotTypesService.call(this, 'slannottypes');
    }

    SpcLinkAnnotTypesService.prototype = Object.create(StudyAnnotTypesService.prototype);

    SpcLinkAnnotTypesService.prototype.getAll = function (studyId) {
      return StudyAnnotTypesService.prototype.getAll.call(this,
                                                          SpecimenLinkAnnotationType,
                                                          studyId);
    };

    SpcLinkAnnotTypesService.prototype.get = function (studyId, annotTypeId) {
      return StudyAnnotTypesService.prototype.get.call(this,
                                                       SpecimenLinkAnnotationType,
                                                       studyId,
                                                       annotTypeId);
    };

    return new SpcLinkAnnotTypesService();
  }

});
