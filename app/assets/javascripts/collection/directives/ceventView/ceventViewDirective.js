/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  /**
   *
   */
  function ceventViewDirective() {
    var directive = {
      restrict: 'EA',
      scope: {},
      bindToController: {
        collectionEventTypes: '=',
        collectionEvent:      '='
      },
      templateUrl : '/assets/javascripts/collection/directives/ceventView/ceventView.html',
      controller: CeventViewCtrl,
      controllerAs: 'vm'
    };
    return directive;
  }

  CeventViewCtrl.$inject = [
    'timeService',
    'modalInput',
    'notificationsService',
    'annotationUpdate'
  ];

  /**
   *
   */
  function CeventViewCtrl(timeService,
                          modalInput,
                          notificationsService,
                          annotationUpdate) {
    var vm = this;

    vm.canUpdateVisitType = (vm.collectionEventTypes.length > 1);
    vm.timeCompletedLocal = timeService.dateToDisplayString(vm.collectionEvent.timeCompleted);
    vm.panelOpen          = true;

    vm.editVisitType      = editVisitType;
    vm.editTimeCompleted  = editTimeCompleted;
    vm.editAnnotation     = editAnnotation;
    vm.panelButtonClicked = panelButtonClicked;

    //--

    function postUpdate(message, title, timeout) {
      return function (cevent) {
        vm.collectionEvent = cevent;
        vm.timeCompletedLocal = timeService.dateToDisplayString(vm.collectionEvent.timeCompleted);
        notificationsService.success(message, title, timeout);
      };
    }

    function editVisitType() {
      if (vm.collectionEventTypes.length <= 1) {
        throw new Error('only a single collection event type is defined for this study');
      }
    }

    function editTimeCompleted() {
      modalInput.dateTime('Update time completed',
                          'Time completed',
                          vm.timeCompletedLocal,
                          { required: true })
        .result.then(function (timeCompleted) {
          vm.collectionEvent.updateTimeCompleted(timeService.dateToUtcString(timeCompleted))
            .then(postUpdate('Time completed updated successfully.', 'Change successful', 1500))
            .catch(notificationsService.updateError);
        });
    }

    function editAnnotation(annotation) {
      annotationUpdate.update(annotation, 'Update ' + annotation.getLabel())
        .then(function (newAnnotation) {
          vm.collectionEvent.addAnnotation(newAnnotation)
            .then(postUpdate('Annotation updated successfully.', 'Change successful', 1500))
            .catch(notificationsService.updateError);
        });
    }

    function panelButtonClicked() {
      vm.panelOpen = !vm.panelOpen;
    }

  }

  return ceventViewDirective;
});
