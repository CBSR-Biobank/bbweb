define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.controller('ParticipantEditCtrl', ParticipantEditCtrl);

  ParticipantEditCtrl.$inject = [
    '$state',
    '$stateParams',
    'participantsService',
    'domainEntityUpdateError',
    'notificationsService',
    'AnnotationHelper',
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
                               AnnotationHelper,
                               study,
                               participant,
                               annotationTypes) {
    var vm = this;

    vm.study           = study;
    vm.participant     = participant;
    vm.submit          = submit;
    vm.cancel          = cancel;
    vm.annotHelpers    = getAnnotationHelpers(annotationTypes);

    if (!vm.participant.id) {
      vm.title = 'Add participant';
      vm.participant.uniqueId = $stateParams.uniqueId;
    } else {
      vm.title = 'Update participant';
    }

    function getAnnotationHelpers(annotationTypes) {
      return _.map(annotationTypes, function(annotType) {
        return new AnnotationHelper(annotType);
      });
    }

    function submit(participant) {
      // convert the form date to data expected by REST API
      participant.studyId = study.id;
      participant.annotations = _.map(vm.annotHelpers, function (annotationHelper) {
        return annotationHelper.getAnnotation();
      });

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

    function submitSuccess(participant) {
      notificationsService.submitSuccess();
      $state.go('home.collection.study.participant',
                {studyId: study.id, participantId: participant.participantId});
    }

    function cancel() {
      $state.go('home.collection.study', {studyId: study.id});
    }
  }

});
