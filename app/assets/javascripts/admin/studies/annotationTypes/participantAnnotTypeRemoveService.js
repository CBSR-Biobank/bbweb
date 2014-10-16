define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.service('participantAnnotTypeRemoveService', participantAnnotTypeRemoveService);

  participantAnnotTypeRemoveService.$inject = [
    'annotationTypeRemoveService',
    'modalService',
    'ParticipantAnnotTypeService'
  ];

  /**
   * Removes a participant annotation type.
   */
  function participantAnnotTypeRemoveService(annotationTypeRemoveService,
                                             modalService,
                                             ParticipantAnnotTypeService) {
    var service = {
      remove: remove
    };
    return service;

    //-------

    function remove(annotType, annotTypesInUse) {
      if (_.contains(annotTypesInUse, annotType.id)) {
        var headerText = 'Cannot remove this annotation type';
        var bodyText = 'This annotation type is in use by participants. ' +
            'If you want to remove the annotation type, ' +
            'it must first be removed from the participants that use it.';
        modalService.modalOk(headerText, bodyText);
      } else {
        annotationTypeRemoveService.remove(
          ParticipantAnnotTypeService.remove,
          annotType,
          'admin.studies.study.participants');
      }
    }
  }

});
