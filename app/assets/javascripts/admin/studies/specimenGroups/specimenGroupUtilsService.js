define(['underscore'], function(_) {
  'use strict';

  specimenGroupUtils.$inject = [
    'domainEntityService',
    'modalService'
  ];

  function specimenGroupUtils(domainEntityService,
                              modalService) {
    var service = {
      inUseModal: inUseModal
    };
    return service;

    //-------

    /**
     * Modal used to confirm user wishes to update or remove a specimen group.
     */
    function inUseModal(specimenGroup, action) {
      return modalService.modalOk(
        'Specimen Group in use',
        'Specimen group <b>' + specimenGroup.name +
          '</b> cannot be ' + action + ' because it is in use by either ' +
          'a collection event type or a specimen link type');
    }

  }

  return specimenGroupUtils;
});
