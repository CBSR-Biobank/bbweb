/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  specimenGroupUtils.$inject = [
    'domainNotificationService',
    'modalService'
  ];

  function specimenGroupUtils(domainNotificationService,
                              modalService) {
    var service = {
      updateInUseModal: updateInUseModal,
      removeInUseModal: removeInUseModal
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

    function updateInUseModal(specimenGroup) {
      return inUseModal(specimenGroup, 'update');
    }

    function removeInUseModal(specimenGroup) {
      return inUseModal(specimenGroup, 'remove');
    }
  }

  return specimenGroupUtils;
});
