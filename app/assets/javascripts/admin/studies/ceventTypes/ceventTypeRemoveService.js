define([], function() {
  'use strict';

  ceventTypeRemoveService.$inject = ['domainEntityRemoveService', 'ceventTypesService'];

  /**
   * Removes a collection event type.
   */
  function ceventTypeRemoveService(domainEntityRemoveService, ceventTypesService) {
    var service = {
      remove: remove
    };
    return service;

    //--

    function remove(ceventType) {
      domainEntityRemoveService.remove(
        'Remove Collection Event Type',
        'Are you sure you want to remove collection event type ' + ceventType.name + '?',
        'Collection event type ' + ceventType.name + ' cannot be removed: ',
        ceventTypesService.remove,
        ceventType,
        'home.admin.studies.study.collection');
    }
  }

});
