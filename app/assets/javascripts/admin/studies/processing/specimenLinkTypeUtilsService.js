/** Common helpers */
define([], function() {
  'use strict';

  specimenLinkTypeUtils.$inject = [
    'domainEntityService', 'spcLinkTypesService'
  ];

  /**
   * Removes a specimen link type.
   */
  function specimenLinkTypeUtils (domainEntityService, spcLinkTypesService) {
    var service = {
      remove: remove
    };
    return service;

    //---

    function remove(spcLinkType) {

      function removeSpecimenLinkType() {
        // FIXME replace with this once specimen group entity is ready specimenGroup.remove();
        return spcLinkTypesService.remove(spcLinkType);
      }

      return domainEntityService.removeEntity(
        removeSpecimenLinkType,
        'Remove Specimen Link Type',
        'Are you sure you want to remove this specimen link type?',
        'Remove Failed',
        'specimen link type ' + spcLinkType.name + ' cannot be removed: ');
    }
  }

  return specimenLinkTypeUtils;
});
