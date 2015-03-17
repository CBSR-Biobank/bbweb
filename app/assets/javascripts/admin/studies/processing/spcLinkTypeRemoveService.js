/** Common helpers */
define([], function() {
  'use strict';

  spcLinkTypeRemoveService.$inject = [
    'domainEntityRemoveService', 'spcLinkTypesService'
  ];

  /**
   * Removes a specimen link type.
   */
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

  return spcLinkTypeRemoveService;
});
