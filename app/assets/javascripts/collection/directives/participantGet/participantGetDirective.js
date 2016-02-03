/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
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
    '$state',
    'stateHelper',
    'modalService',
    'Participant'
  ];

  /**
   *
   */
  function ParticipantGetCtrl($state,
                              stateHelper,
                              modalService,
                              Participant) {
    var vm = this;

    vm.uniqueId = '';
    vm.uniqueIdChanged = uniqueIdChanged;

    //--

    function uniqueIdChanged() {
      if (vm.uniqueId.length <= 0) {
        // dont do anything if user has not entered any text
        return;
      }

      Participant.getByUniqueId(vm.study.id, vm.uniqueId)
        .then(function (participant) {
          $state.go('home.collection.study.participant.summary', { participantId: participant.id });
        })
        .catch(function (error) {
          if (error.status === 404) {
            createParticipantModal(vm.uniqueId);
          } else {
            throw new Error('ParticipantGetCtrl:' + JSON.stringify(error));
          }
        });
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
          $state.go('home.collection.study.addParticipant', { uniqueId: uniqueId });
        })
        .catch(function() {
          stateHelper.reloadAndReinit();
        });
    }
  }

  return participantGetDirective;

});
