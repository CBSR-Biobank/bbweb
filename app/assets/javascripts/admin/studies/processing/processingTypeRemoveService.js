define(['../../module'], function(module) {
  'use strict';

  module.service('processingTypeRemoveService', processingTypeRemoveService);

  processingTypeRemoveService.$inject = ['domainEntityRemoveService', 'processingTypesService'];

  /**
   * Removes a processing type.
   */
  function processingTypeRemoveService(domainEntityRemoveService, processingTypesService) {
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
        processingTypesService.remove,
        processingType,
        'admin.studies.study.processing');
    }
  }

});
