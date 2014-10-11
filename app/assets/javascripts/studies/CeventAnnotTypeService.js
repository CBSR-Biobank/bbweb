define(['./module'], function(module) {
  'use strict';

  module.service('CeventAnnotTypeService', CeventAnnotTypeService);

  CeventAnnotTypeService.$inject = ['StudyAnnotTypeService'];

  /**
   * Service to access Collection Event Annotation Types.
   */
  function CeventAnnotTypeService(StudyAnnotTypeService) {
    var baseUrl = '/studies/ceannottypes';
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
