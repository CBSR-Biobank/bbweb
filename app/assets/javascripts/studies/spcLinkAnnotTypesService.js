define(['./module'], function(module) {
  'use strict';

  module.service('spcLinkAnnotTypesService', SpcLinkAnnotTypesService);

  SpcLinkAnnotTypesService.$inject = ['studyAnnotTypesService'];

  /**
   * Service to access Specimen Link Annotation Types.
   */
  function SpcLinkAnnotTypesService(studyAnnotTypesService) {
    var annotTypeUri = 'slannottypes';
    var service = {
      getAll      : getAll,
      get         : get,
      addOrUpdate : addOrUpdate,
      remove      : remove
    };
    return service;

    //-------

    function getAll(studyId) {
      return studyAnnotTypesService.getAll(annotTypeUri, studyId);
    }

    function get(studyId, annotTypeId) {
      return studyAnnotTypesService.get(annotTypeUri, studyId, annotTypeId);
    }

    function addOrUpdate(annotType) {
      return studyAnnotTypesService.addOrUpdate(annotTypeUri, annotType);
    }

    function remove(annotType) {
      return studyAnnotTypesService.remove(annotTypeUri, annotType);
    }

  }

});
