/* global define */
define([], function() {
  'use strict';

  CollectionStudyCtrl.$inject = [
    '$state',
    'stateHelper',
    'modalService',
    'participantsService',
    'study'
  ];

  /**
   *
   */
  function CollectionStudyCtrl($state,
                               stateHelper,
                               modalService,
                               participantsService,
                               study) {
    var vm = this;

    vm.study = study;
    vm.uniqueId = '';
    vm.uniqueIdChanged = uniqueIdChanged;

    function uniqueIdChanged() {
      if (vm.uniqueId.length <= 0) {
        // dont do anything if user has not entered any text
        return;
      }

      participantsService.getByUniqueId(vm.study.id, vm.uniqueId)
        .then(function (participant) {
          $state.go('home.collection.study.participant', { participantId: participant.id });
        })
        .catch(function (error) {
          if (error.status === 404) {
            createParticipantModal(vm.uniqueId);
          } else {
            throw new Error('CollectionStudyCtrl:' + JSON.stringify(error));
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

  return CollectionStudyCtrl;
});
