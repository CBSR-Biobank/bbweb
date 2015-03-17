define([], function() {
  'use strict';

  ParticipantEditCtrl.$inject = [
    '$state',
    '$stateParams',
    'participantsService',
    'domainEntityUpdateError',
    'notificationsService',
    'Participant',
    'study',
    'participant',
    'annotationTypes'
  ];

  /**
   * This controller is used for adding or editing a participant.
   */
  function ParticipantEditCtrl($state,
                               $stateParams,
                               participantsService,
                               domainEntityUpdateError,
                               notificationsService,
                               Participant,
                               study,
                               participant,
                               annotationTypes) {
    var vm = this;

    vm.study           = study;
    vm.participant     = new Participant(study, participant, annotationTypes);
    vm.submit          = submit;
    vm.cancel          = cancel;

    vm.onSubmitState = {
      name: 'home.collection.study.participant',
      params: {
        studyId: study.id,
        participantId: participant.id
      }
    };

    if (vm.participant.isNew) {
      vm.title = 'Add participant';
      vm.participant.uniqueId = $stateParams.uniqueId;
      vm.onCancelState = {name: 'home.collection.study', params: {studyId: study.id}};
    } else {
      vm.title = 'Update participant';
      vm.onCancelState = vm.onSubmitState;
    }

    function submit(participant) {
      // convert the data from the form to data expected by REST API
      participant.updateAnnotations();

      participantsService.addOrUpdate(participant)
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityUpdateError.handleError(
            error,
            'participant',
            'home.collection.study',
            {studyId: study.id});
        });
    }

    function gotoState(state, reload) {
      $state.transitionTo(state.name, state.params, { reload: reload });
    }

    function submitSuccess(event) {
      if (vm.participant.isNew) {
        // the event contains the id assigned to this new participant, therefore, the state data can be
        // updated
        vm.onSubmitState.params.participantId = event.participantId;
      }
      notificationsService.submitSuccess();
      gotoState(vm.onSubmitState, true);
    }

    function cancel() {
      gotoState(vm.onCancelState, false);
    }
  }

  return ParticipantEditCtrl;
});
