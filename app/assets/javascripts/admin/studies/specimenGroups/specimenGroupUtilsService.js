define(['underscore'], function(_) {
  'use strict';

  specimenGroupUtils.$inject = [
    'domainEntityService',
    'modalService'
  ];

  /**
   * Removes a specimen group.
   */
  function specimenGroupUtils(domainEntityService,
                              modalService) {
    var service = {
      inUseModal: inUseModal,
      remove: remove
    };
    return service;

    //-------

    function inUseModal(specimenGroup) {
      return modalService.modalOk(
        'Specimen Group in use',
        'Specimen group <b>' + specimenGroup.name +
          '</b> cannot be removed because it is in use by either ' +
          'a collection event type or a specimen link type');
    }

    function remove(specimenGroup) {
      return domainEntityService.removeEntity(
        specimenGroup.remove,
        'Remove Specimen Group',
        'Are you sure you want to remove specimen group ' + specimenGroup.name + '?',
        'Remove Failed',
        'Specimen group ' + specimenGroup.name + ' cannot be removed: ');
    }

  }

  return specimenGroupUtils;
});
