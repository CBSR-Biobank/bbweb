define(['./module'], function(module) {
  'use strict';

  module.service('ceventAnnotTypesService', CeventAnnotTypesService);

  CeventAnnotTypesService.$inject = ['studyAnnotTypesService'];

  /**
   * Service to access Collection Event Annotation Types.
   */
  function CeventAnnotTypesService(studyAnnotTypesService) {
    var annotTypeUri ='ceannottypes';

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
