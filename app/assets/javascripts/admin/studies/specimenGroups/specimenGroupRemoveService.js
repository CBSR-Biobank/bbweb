define(['../../module'], function(module) {
  'use strict';

  module.service('specimenGroupRemoveService', specimenGroupRemoveService);

  specimenGroupRemoveService.$inject = ['domainEntityRemoveService', 'SpecimenGroupService'];

  /**
   * Removes a specimen group.
   */
  function specimenGroupRemoveService(domainEntityRemoveService, SpecimenGroupService) {
    var service = {
      remove: remove
    };
    return service;

    //-------

    function remove(specimenGroup) {
      domainEntityRemoveService.remove(
        'Remove Specimen Group',
        'Are you sure you want to remove specimen group ' + specimenGroup.name + '?',
        'Specimen group ' + specimenGroup.name + ' cannot be removed: ',
        SpecimenGroupService.remove,
        specimenGroup,
        'admin.studies.study.specimens');
    }

  }

});
