/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/collection/components/participantSummary/participantSummary.html',
    controller: ParticipantSummaryController,
    controllerAs: 'vm',
    bindings: {
      study:       '<',
      participant: '<'
    }
  };

  ParticipantSummaryController.$inject = [
    'gettextCatalog',
    'annotationUpdate',
    'notificationsService',
    'modalInput'
  ];

  /*
   * Controller for this component.
   */
  function ParticipantSummaryController(gettextCatalog,
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
      modalInput.text(gettextCatalog.getString('Update unique ID'),
                      gettextCatalog.getString('Unique ID'),
                      vm.participant.uniqueId,
                      { required: true }).result
        .then(function (uniqueId) {
          vm.participant.updateUniqueId(uniqueId)
            .then(postUpdate(gettextCatalog.getString('Unique ID updated successfully.'),
                             gettextCatalog.getString('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

    function editAnnotation(annotation) {
      annotationUpdate.update(annotation).then(function (newAnnotation) {
        vm.participant.addAnnotation(newAnnotation)
          .then(postUpdate(gettextCatalog.getString('Annotation updated successfully.'),
                           gettextCatalog.getString('Change successful')))
          .catch(notificationsService.updateError);
      });
    }

    function getAnnotationUpdateButtonTitle(annotation) {
      /// label is a name assigned by the user for an annotation type
      return gettextCatalog.getString('Update {{label}}', { label: annotation.getLabel() });
    }
  }

  return component;
});
