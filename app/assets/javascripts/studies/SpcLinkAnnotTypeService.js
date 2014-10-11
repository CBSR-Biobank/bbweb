define(['./module'], function(module) {
  'use strict';

  module.service('SpcLinkAnnotTypeService', SpcLinkAnnotTypeService);

  SpcLinkAnnotTypeService.$inject = ['StudyAnnotTypeService'];

  /**
   * Service to access Specimen Link Annotation Types.
   */
  function SpcLinkAnnotTypeService(StudyAnnotTypeService) {
    var baseUrl = '/studies/slannottypes';
    var service = {
      getAll      : getAll,
      get         : get,
      addOrUpdate : addOrUpdate,
      remove      : remove
    };
    return service;

    //-------

    function getAll(studyId) {
      return StudyAnnotTypeService.getAll(baseUrl, studyId);
    }

    function get(studyId, annotTypeId) {
      return StudyAnnotTypeService.get(baseUrl, studyId, annotTypeId);
    }

    function addOrUpdate(annotType) {
      return StudyAnnotTypeService.addOrUpdate(baseUrl, annotType);
    }

    function remove(annotType) {
      return StudyAnnotTypeService.remove(baseUrl, annotType);
    }

  }

});
