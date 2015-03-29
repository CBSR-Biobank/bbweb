define([], function() {
  'use strict';

  ceventTypeUtils.$inject = ['domainEntityService', 'ceventTypesService'];

  /**
   * Removes a collection event type.
   */
  function ceventTypeUtils(domainEntityService, ceventTypesService) {
    var service = {
      remove: remove
    };
    return service;

    //--

    function remove(ceventType) {

      function removeCeventType() {
        // FIXME replace with this once specimen group entity is ready specimenGroup.remove();
        return ceventTypesService.remove(ceventType);
      }

      return domainEntityService.removeEntity(
        removeCeventType,
        'Remove Collection Event Type',
        'Are you sure you want to remove collection event type ' + ceventType.name + '?',
        'Remove Failed',
        'Collection event type ' + ceventType.name + ' cannot be removed: ');
    }
  }

  return ceventTypeUtils;
});
