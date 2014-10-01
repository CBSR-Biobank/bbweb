define(['../../module'], function(module) {
  'use strict';

  module.controller('SpecimenGroupEditCtrl', SpecimenGroupEditCtrl);

  SpecimenGroupEditCtrl.$inject = [
    '$state',
    'modelObjUpdateError',
    'SpecimenGroupService',
    'valueTypes',
    'study',
    'specimenGroup'
  ];

  /**
   * Add or update an specimen Group.
   */
  function SpecimenGroupEditCtrl($state,
                                 modelObjUpdateError,
                                 SpecimenGroupService,
                                 valueTypes,
                                 study,
                                 specimenGroup) {
    var action = specimenGroup.id ? 'Update' : 'Add';

    var vm = this;
    vm.title =  action + ' Specimen Group';
    vm.study = study;
    vm.specimenGroup = specimenGroup;

    vm.anatomicalSourceTypes = valueTypes.anatomicalSourceType.sort();
    vm.preservTypes          = valueTypes.preservationType.sort();
    vm.preservTempTypes      = valueTypes.preservationTemperatureType.sort();
    vm.specimenTypes         = valueTypes.specimenType.sort();

    vm.submit = submit;
    vm.cancel = cancel;

    //--

    function gotoReturnState() {
      return $state.go('admin.studies.study.specimens', {}, {reload: true});
    }

    function submit(specimenGroup) {
      SpecimenGroupService.addOrUpdate(specimenGroup)
        .then(gotoReturnState)
        .catch(function(error) {
          modelObjUpdateError.handleError(
            error,
            'specimen link type',
            'admin.studies.study.specimens',
            {},
            {reload: true});
        });
    }

    function cancel() {
      gotoReturnState();
    }

  }

});
