define([], function() {
  'use strict';

  processingTypeUtils.$inject = ['domainEntityService', 'processingTypesService'];

  /**
   * Removes a processing type.
   */
  function processingTypeUtils(domainEntityService, processingTypesService) {
    var service = {
      remove: remove
    };
    return service;

    //-------

    function remove(processingType) {

      function removeProcessingType() {
        // FIXME replace with this once specimen group entity is ready specimenGroup.remove();
        return processingTypesService.remove(processingType);
      }

      return domainEntityService.removeEntity(
        removeProcessingType,
        'Remove Processing Type',
        'Are you sure you want to remove processing type ' + processingType.name + '?',
        'Remove Failed',
        'Processing type ' + processingType.name + ' cannot be removed: ');
    }
  }

  return processingTypeUtils;
});
