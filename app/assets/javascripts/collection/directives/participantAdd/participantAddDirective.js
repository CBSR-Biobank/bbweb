/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  /**
   * Description
   */
  function participantAddDirective() {
    var directive = {
      scope: {},
      bindToController: {
        study:    '=',
        uniqueId: '@'
      },
      restrict: 'E',
      templateUrl : '/assets/javascripts/collection/directives/participantAdd/participantAdd.html',
      controller: ParticipantAddCtrl,
      controllerAs: 'vm'
    };
    return directive;
  }

  ParticipantAddCtrl.$inject = [
    '$state',
    'Participant',
    'domainEntityService',
    'notificationsService'
  ];

  /**
   * This controller is used for adding or editing a participant.
   */
  function ParticipantAddCtrl($state,
                              Participant,
                              domainEntityService,
                              notificationsService) {
    var vm = this;

    vm.title       = 'Add participant';
    vm.participant = new Participant({ uniqueId: vm.uniqueId }, vm.study);
    vm.submit      = submit;
    vm.cancel      = cancel;

    function submit(participant) {
      // convert the data from the form to data expected by REST API
      participant.add()
        .then(submitSuccess)
        .catch(function(error) {
          return domainEntityService.updateErrorModal(error, 'participant');
        }).catch(function () {
          $state.go('home.collection.study', { studyId: vm.study.id });
        });
    }

    function submitSuccess(reply) {
      // the reply contains the id assigned to this new participant, therefore, the state data can be
      // updated
      notificationsService.submitSuccess();
      $state.go(
        'home.collection.study.participant.summary',
        { studyId: vm.study.id, participantId: reply.id },
        { reload: true });
    }

    function cancel() {
      $state.go('home.collection.study', { studyId: vm.study.id });
    }
  }

  return participantAddDirective;
});
