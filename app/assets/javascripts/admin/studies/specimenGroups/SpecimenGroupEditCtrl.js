/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
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
    var vm = this,
        possibleReturnStateNames = [
          'home.admin.studies.study.collection.view',
          'home.admin.studies.study.processing'
        ],
        returnState;

    returnState = determineReturnState();

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

    /**
     * Determines the state to transition to when the user submits the form or cancels it.
     */
    function determineReturnState() {
      var stateParams = {},
          returnStateName = _.filter(possibleReturnStateNames, function(name) {
            return ($state.current.name.indexOf(name) >= 0);
          });

      if (returnStateName.length !== 1) {
        throw new Error('invalid current state name: ' + $state.current.name);
      }

      return {
        name:    _.first(returnStateName),
        params:  stateParams,
        options: { reload: true }
      };
    }

    function gotoReturnState(state) {
      return $state.go(state.name, state.params, state.options);
    }

    function submitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState(returnState);
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
      gotoReturnState(returnState);
    }

  }

  return SpecimenGroupEditCtrl;
});
