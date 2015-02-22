define(['./module'], function(module) {
  'use strict';

  module.service('ceventAnnotTypesService', ceventAnnotTypesService);

  ceventAnnotTypesService.$inject = [
    'StudyAnnotTypesService',
    'CollectionEventAnnotationType'
  ];

  /**
   * Service to access Collection Event Annotation Types.
   */
  function ceventAnnotTypesService(StudyAnnotTypesService,
                                   CollectionEventAnnotationType) {

    function CeventAnnotTypesService() {
      StudyAnnotTypesService.call(this, 'ceannottypes');
    }

    CeventAnnotTypesService.prototype = Object.create(StudyAnnotTypesService.prototype);

    CeventAnnotTypesService.prototype.getAll = function (studyId) {
      return StudyAnnotTypesService.prototype.getAll.call(this,
                                                          CollectionEventAnnotationType,
                                                          studyId);
    };

    CeventAnnotTypesService.prototype.get = function (studyId, annotTypeId) {
      return StudyAnnotTypesService.prototype.get.call(this,
                                                       CollectionEventAnnotationType,
                                                       studyId,
                                                       annotTypeId);
    };

    return new CeventAnnotTypesService();

  }

});
