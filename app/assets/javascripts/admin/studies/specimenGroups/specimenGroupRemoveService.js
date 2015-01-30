define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.service('specimenGroupRemoveService', specimenGroupRemoveService);

  specimenGroupRemoveService.$inject = [
    'domainEntityRemoveService',
    'modalService',
    'specimenGroupsService'
  ];

  /**
   * Removes a specimen group.
   */
  function specimenGroupRemoveService(domainEntityRemoveService, modalService, specimenGroupsService) {
    var service = {
      remove: remove
    };
    return service;

    //-------

    function remove(specimenGroup, specimenGroupIdsInUse) {
      if (_.contains(specimenGroupIdsInUse, specimenGroup.id)) {
        modalService.modalOk(
          'Specimen Group in use',
          'This specimen group cannot be removed because it is in use by either ' +
            'a collection event type or a specimen link type');
      } else {
        domainEntityRemoveService.remove(
          'Remove Specimen Group',
          'Are you sure you want to remove specimen group ' + specimenGroup.name + '?',
          'Specimen group ' + specimenGroup.name + ' cannot be removed: ',
          specimenGroupsService.remove,
          specimenGroup,
          'home.admin.studies.study.specimens');
      }
    }
  }

});
