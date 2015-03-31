define([], function() {
  'use strict';

  ceventTypeUtils.$inject = ['domainEntityService'];

  /**
   * Removes a collection event type.
   */
  function ceventTypeUtils(domainEntityService) {
    var service = {
      remove: remove
    };
    return service;

    //--

    function remove(ceventType) {

      return domainEntityService.removeEntity(
        ceventType.remove,
        'Remove Collection Event Type',
        'Are you sure you want to remove collection event type ' + ceventType.name + '?',
        'Remove Failed',
        'Collection event type ' + ceventType.name + ' cannot be removed: ');
    }
  }

  return ceventTypeUtils;
});
