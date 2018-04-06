/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 * Add or update an specimen Group.
 */
/* @ngInject */
function SpecimenGroupEditCtrl($state,                            // eslint-disable-line no-unused-vars
                               domainNotificationService,
                               notificationsService,
                               AnatomicalSourceType,
                               PreservationType,
                               PreservationTemperature,
                               SpecimenType,
                               study,
                               specimenGroup) {
  var vm = this,
      possibleReturnStateNames = [
        'home.admin.studies.study.collection.ceventType',
        'home.admin.studies.study.processing'
      ],
      returnState;

  returnState = determineReturnState();

  vm.title                 = (specimenGroup.isNew() ? 'Add' : 'Update') + ' Specimen Group';
  vm.study                 = study;
  vm.specimenGroup         = specimenGroup;
  vm.anatomicalSourceTypes = Object.values(AnatomicalSourceType);
  vm.preservTypes          = Object.values(PreservationType);
  vm.preservTempTypes      = Object.values(PreservationTemperature);
  vm.specimenTypes         = Object.values(SpecimenType);

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
        domainNotificationService.updateErrorModal(
          error,'specimen group');
      });
  }

  function cancel() {
    gotoReturnState(returnState);
  }

}

// TEMP: don't add this controller for now
export default () => {}
