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
    'gettext',
    'gettextCatalog',
    'annotationUpdate',
    'notificationsService',
    'modalInput'
  ];

  /**
   *
   */
  function ParticipantSummaryCtrl(gettext,
                                  gettextCatalog,
                                  annotationUpdate,
                                  notificationsService,
                                  modalInput) {
    var vm = this;

    vm.editUniqueId                   = editUniqueId;
    vm.editAnnotation                 = editAnnotation;
    vm.getAnnotationUpdateButtonTitle = getAnnotationUpdateButtonTitle;

    //---

    function postUpdate(message, title, timeout) {
      timeout = timeout || 1500;
      return function (participant) {
        vm.participant = participant;
        notificationsService.success(message, title, timeout);
      };
    }

    function editUniqueId() {
      modalInput.text(gettext('Update unique ID'),
                      gettext('Unique ID'),
                      vm.participant.uniqueId,
                      { required: true }).result
        .then(function (uniqueId) {
          vm.participant.updateUniqueId(uniqueId)
            .then(postUpdate(gettext('Unique ID updated successfully.'),
                             gettext('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

    function editAnnotation(annotation) {
      annotationUpdate.update(annotation).then(function (newAnnotation) {
        vm.participant.addAnnotation(newAnnotation)
          .then(postUpdate(gettext('Annotation updated successfully.'),
                           gettext('Change successful')))
          .catch(notificationsService.updateError);
      });
    }

    function getAnnotationUpdateButtonTitle(annotation) {
      /// label is a name assigned by the user for an annotation type
      return gettextCatalog.getString('Update {{label}}', { label: annotation.getLabel() });
    }
  }


  return participantSummaryDirective;
});
