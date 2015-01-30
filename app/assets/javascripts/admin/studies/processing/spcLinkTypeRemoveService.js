/** Common helpers */
define(['../../module'], function(module) {
  'use strict';

  /**
   * Removes a specimen link type.
   */
  module.service('spcLinkTypeRemoveService', spcLinkTypeRemoveService);

  spcLinkTypeRemoveService.$inject = [
    'domainEntityRemoveService', 'spcLinkTypesService'
  ];

  function spcLinkTypeRemoveService (domainEntityRemoveService, spcLinkTypesService) {
    var service = {
      remove: remove
    };
    return service;

    //---

    function remove(spcLinkType) {
      domainEntityRemoveService.remove(
        'Remove Specimen Link Type',
        'Are you sure you want to remove specimen link type ' + spcLinkType.name + '?',
        'specimen link type ' + spcLinkType.name + ' cannot be removed: ',
        spcLinkTypesService.remove,
        spcLinkType,
        'home.admin.studies.study.processing');
    }
  }

});
