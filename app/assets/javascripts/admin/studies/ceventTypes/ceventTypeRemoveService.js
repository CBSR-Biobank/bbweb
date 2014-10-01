define(['../../module'], function(module) {
  'use strict';

  /**
   * Removes a collection event type.
   */
  module.service('ceventTypeRemoveService', ceventTypeRemoveService);

  ceventTypeRemoveService.$inject = ['modelObjRemoveService', 'CeventTypeService'];

  function ceventTypeRemoveService(modelObjRemoveService, CeventTypeService) {
    var service = {
      remove: remove
    };
    return service;

    //--

    function remove(ceventType) {
      modelObjRemoveService.remove(
        'Remove Collection Event Type',
        'Are you sure you want to remove collection event type ' + ceventType.name + '?',
        'Collection event type ' + ceventType.name + ' cannot be removed: ',
        CeventTypeService.remove,
        ceventType,
        'admin.studies.study.collection');
    }
  }

});
