define(['../../module'], function(module) {
  'use strict';

  module.service('processingTypeRemoveService', processingTypeRemoveService);

  processingTypeRemoveService.$inject = ['domainEntityRemoveService', 'ProcessingTypeService'];

  /**
   * Removes a processing type.
   */
  function processingTypeRemoveService(domainEntityRemoveService, ProcessingTypeService) {
    var service = {
      remove: remove
    };
    return service;

    //-------

    function remove(processingType) {
      domainEntityRemoveService.remove(
        'Remove Processing Type',
        'Are you sure you want to remove processing type ' + processingType.name + '?',
        'Processing type ' + processingType.name + ' cannot be removed: ',
        ProcessingTypeService.remove,
        processingType,
        'admin.studies.study.processing');
    }
  }

});
