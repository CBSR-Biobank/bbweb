/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  SpecimenGroupEditCtrl.$inject = [
    '$state',
    'domainEntityService',
    'notificationsService',
    'AnatomicalSourceType',
    'PreservationType',
    'PreservationTemperatureType',
    'SpecimenType',
    'study',
    'specimenGroup'
  ];

  /**
   * Add or update an specimen Group.
   */
  function SpecimenGroupEditCtrl($state,
                                 domainEntityService,
                                 notificationsService,
                                 AnatomicalSourceType,
                                 PreservationType,
                                 PreservationTemperatureType,
                                 SpecimenType,
                                 study,
                                 specimenGroup) {
    var vm = this;

    vm.title                 = (specimenGroup.isNew() ? 'Add' : 'Update') + ' Specimen Group';
    vm.study                 = study;
    vm.specimenGroup         = specimenGroup;
    vm.anatomicalSourceTypes = AnatomicalSourceType.values();
    vm.preservTypes          = PreservationType.values();
    vm.preservTempTypes      = PreservationTemperatureType.values();
    vm.specimenTypes         = SpecimenType.values();

    vm.submit = submit;
    vm.cancel = cancel;

    //--

    function gotoReturnState() {
      return $state.go('home.admin.studies.study.specimens', {}, {reload: true});
    }

    function submitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState();
    }

    function submit(specimenGroup) {
      specimenGroup.addOrUpdate()
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityService.updateErrorModal(
            error,'specimen group');
        });
    }

    function cancel() {
      gotoReturnState();
    }

  }

  return SpecimenGroupEditCtrl;
});
