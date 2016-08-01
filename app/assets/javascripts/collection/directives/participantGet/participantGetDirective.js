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
                              stateHelper,
                              modalService,
                              Participant) {
    var vm = this;

    vm.uniqueId = '';
    vm.uniqueIdChanged = uniqueIdChanged;

    //--

    function uniqueIdChanged() {
      var studyMismatchRe = /EntityCriteriaError.participant not in study/i;
      if (vm.uniqueId.length > 0) {
        Participant.getByUniqueId(vm.study.id, vm.uniqueId)
          .then(function (participant) {
            $state.go('home.collection.study.participant.summary', { participantId: participant.id });
          })
          .catch(function (error) {
            if (error.status === 404) {
              createParticipantModal(vm.uniqueId);
            } else if ((error.status === 400) && error.data.message.match(studyMismatchRe)) {
              modalService.modalOk(
                'Duplicate unique ID',
                'Unique ID <strong>' + vm.uniqueId + '</strong> is already in use by a participant ' +
                  'in another study. Please use a diffent one.')
                .then(function () {
                  vm.uniqueId = undefined;
                });
            } else {
              $log.error('could not get participant by uniqueId: ', JSON.stringify(error));
            }
          });
      }
    }

    function createParticipantModal(uniqueId) {
      var modalDefaults = {};
      var modalOptions = {
        headerHtml: 'Create participant',
        bodyHtml: 'Would you like to create participant with unique ID <strong>' + uniqueId + '</strong>?',
        closeButtonText: 'Cancel',
        actionButtonText: 'OK'
      };

      modalService.showModal(modalDefaults, modalOptions)
        .then(function() {
          $state.go('home.collection.study.participantAdd', { uniqueId: uniqueId });
        })
        .catch(function() {
          stateHelper.reloadAndReinit();
        });
    }
  }

  return participantGetDirective;

});
