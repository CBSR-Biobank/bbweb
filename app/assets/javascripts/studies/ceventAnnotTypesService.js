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

    return new CeventAnnotTypesService();
  }

});
