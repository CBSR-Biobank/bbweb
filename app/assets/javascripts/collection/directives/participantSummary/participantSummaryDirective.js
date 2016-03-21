/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function participantSummaryDirective() {
    var directive = {
      restrict: 'EA',
      scope: {},
      bindToController: {
        study: '=',
        participant: '='
      },
      templateUrl : '/assets/javascripts/collection/directives/participantSummary/participantSummary.html',
      controller: ParticipantSummaryCtrl,
      controllerAs: 'vm'
    };
    return directive;

  }

  ParticipantSummaryCtrl.$inject = [
    'annotationUpdate',
    'notificationsService',
    'modalInput'
  ];

  /**
   *
   */
  function ParticipantSummaryCtrl(annotationUpdate,
                                  notificationsService,
                                  modalInput) {
    var vm = this;

    vm.editUniqueId   = editUniqueId;
    vm.editAnnotation = editAnnotation;

    //---

    function postUpdate(message, title, timeout) {
      return function (participant) {
        vm.participant = participant;
        notificationsService.success(message, title, timeout);
      };
    }

    function editUniqueId() {
      modalInput.text('Update unique ID', 'Unique ID', vm.participant.uniqueId, { required: true })
        .then(function (uniqueId) {
          vm.participant.updateUniqueId(uniqueId)
            .then(postUpdate('Unique ID updated successfully.', 'Change successful', 1500))
            .catch(notificationsService.updateError);
        });
    }

    function editAnnotation(annotation) {
      annotationUpdate.update(annotation).then(function (newAnnotation) {
        vm.participant.addAnnotation(newAnnotation)
          .then(postUpdate('Annotation updated successfully.', 'Change successful', 1500))
          .catch(notificationsService.updateError);
      });
    }
  }


  return participantSummaryDirective;
});
