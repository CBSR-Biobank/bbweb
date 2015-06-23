/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  ParticipantEditCtrl.$inject = [
    '$state',
    '$stateParams',
    'domainEntityService',
    'notificationsService',
    'study',
    'participant',
    'annotationTypes'
  ];

  /**
   * This controller is used for adding or editing a participant.
   */
  function ParticipantEditCtrl($state,
                               $stateParams,
                               domainEntityService,
                               notificationsService,
                               study,
                               participant,
                               annotationTypes) {
    var vm = this;

    vm.study       = study;
    vm.participant = participant;
    vm.submit      = submit;
    vm.cancel      = cancel;

    console.log(participant);

    vm.returnState = {
      name: 'home.collection.study.participant',
      params: {
        studyId: study.id,
        participantId: participant.id
      },
      options: { reload: true }
    };

    if (vm.participant.isNew()) {
      vm.title = 'Add participant';
      vm.participant.uniqueId = $stateParams.uniqueId;
      vm.onCancelState = {name: 'home.collection.study', params: {studyId: study.id}};
    } else {
      vm.title = 'Update participant';
      vm.onCancelState = _.extend({}, vm.returnState, { reload: false });
    }

    function submit(participant) {
      // convert the data from the form to data expected by REST API
      participant.addOrUpdate()
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityService.updateErrorModal(
            error, 'participant').catch(function () {
              $state.go('home.collection.study', {studyId: study.id});
            });
        });
    }

    function gotoState(state) {
      $state.go(state.name, state.params, state.options);
    }

    function submitSuccess(reply) {
      if (vm.participant.isNew()) {
        // the reply contains the id assigned to this new participant, therefore, the state data can be
        // updated
        vm.returnState.params.participantId = reply.id;
      }
      notificationsService.submitSuccess();
      gotoState(vm.returnState);
    }

    function cancel() {
      gotoState(vm.onCancelState);
    }
  }

  return ParticipantEditCtrl;
});
