/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Description
   */
  function participantGetDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study: '='
      },
      templateUrl : '/assets/javascripts/collection/directives/participantGet/participantGet.html',
      controller: ParticipantGetCtrl,
      controllerAs: 'vm'
    };
    return directive;

  }

  ParticipantGetCtrl.$inject = [
    '$q',
    '$log',
    '$state',
    'gettextCatalog',
    'stateHelper',
    'modalService',
    'Participant'
  ];

  /**
   *
   */
  function ParticipantGetCtrl($q,
                              $log,
                              $state,
                              gettextCatalog,
                              stateHelper,
                              modalService,
                              Participant) {
    var vm = this;

    vm.uniqueId = '';
    vm.onSubmit = onSubmit;

    //--

    function onSubmit() {
      if (vm.uniqueId.length > 0) {
        Participant.getByUniqueId(vm.study.id, vm.uniqueId)
          .then(function (participant) {
            $state.go('home.collection.study.participant.summary', { participantId: participant.id });
          })
          .catch(participantGetError);
      }
    }

    function participantGetError(error) {
      var studyMismatchRe = /EntityCriteriaError.participant not in study/i;
      if (error.status === 404) {
        createParticipantModal(vm.uniqueId);
      } else if ((error.status === 400) && error.data.message.match(studyMismatchRe)) {
        modalService.modalOk(
          gettextCatalog.getString('Duplicate unique ID'),
          gettextCatalog.getString(
          'Unique ID <strong>{{id}}</strong> is already in use by a participant ' +
              'in another study. Please use a diffent one.',
            { id: vm.uniqueId }))
          .then(function () {
            vm.uniqueId = '';
          });
      } else {
          $log.error('could not get participant by uniqueId: ', JSON.stringify(error));
      }
    }

    function createParticipantModal(uniqueId) {
      modalService.modalOkCancel(
        gettextCatalog.getString('Create participant'),
        gettextCatalog.getString(
          'Would you like to create participant with unique ID <strong>{{id}}</strong>?',
          { id: uniqueId })
      ).then(function() {
        $state.go('home.collection.study.participantAdd', { uniqueId: uniqueId });
      }).catch(function() {
        stateHelper.reloadAndReinit();
      });
    }
  }

  return participantGetDirective;

});
