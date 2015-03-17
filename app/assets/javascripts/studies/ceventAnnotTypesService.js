define([], function() {
  'use strict';

  ceventAnnotTypesFactory.$inject = [
    'StudyAnnotTypesService'
  ];

  /**
   * Service to access Collection Event Annotation Types.
   */
  function ceventAnnotTypesFactory(StudyAnnotTypesService) {

    function CeventAnnotTypesService() {
      StudyAnnotTypesService.call(this, 'ceannottypes');
    }

    CeventAnnotTypesService.prototype = Object.create(StudyAnnotTypesService.prototype);

    return new CeventAnnotTypesService();
  }

  return ceventAnnotTypesFactory;
});
