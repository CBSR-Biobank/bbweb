/** Common helpers */
define([], function() {
  'use strict';

  specimenLinkTypeUtils.$inject = [
    'domainEntityService'
  ];

  /**
   * Removes a specimen link type.
   */
  function specimenLinkTypeUtils (domainEntityService) {
    var service = {
      remove: remove
    };
    return service;

    //---

    function remove(spcLinkType) {

      return domainEntityService.removeEntity(
        spcLinkType.remove,
        'Remove Specimen Link Type',
        'Are you sure you want to remove this specimen link type?',
        'Remove Failed',
        'specimen link type ' + spcLinkType.name + ' cannot be removed: ');
    }
  }

  return specimenLinkTypeUtils;
});
