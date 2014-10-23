define(['../../module'], function(module) {
  'use strict';

  /**
   * Removes a collection event type.
   */
  module.service('ceventTypeRemoveService', ceventTypeRemoveService);

  ceventTypeRemoveService.$inject = ['domainEntityRemoveService', 'ceventTypesService'];

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
        'admin.studies.study.collection');
    }
  }

});
