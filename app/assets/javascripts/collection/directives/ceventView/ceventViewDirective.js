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
        study:                '=',
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
    '$state',
    'Specimen',
    'timeService',
    'modalService',
    'modalInput',
    'domainEntityService',
    'notificationsService',
    'annotationUpdate'
  ];

  /**
   *
   */
  function CeventViewCtrl($state,
                          Specimen,
                          timeService,
                          modalService,
                          modalInput,
                          domainEntityService,
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
    vm.remove             = remove;

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

    function remove() {
      Specimen.list(vm.collectionEvent.id).then(function (paginatedUsers) {
        if (paginatedUsers.items.length > 0) {
          modalService.modalOk(
            'Cannot remove collection event',
            'This collection event has specimens. Please remove the specimens first.');
        } else {
          domainEntityService.removeEntity(
            promiseFn,
            'Remove specimen',
            'Are you sure you want to remove collection event with visit # <strong>' + vm.collectionEvent.visitNumber + '</strong>?',
            'Remove failed',
            'Collection event with visit number ' + vm.collectionEvent.visitNumber + ' cannot be removed');
        }

        function promiseFn() {
          return vm.collectionEvent.remove().then(function () {
            notificationsService.success('Collection event removed');
            $state.go('home.collection.study.participant.cevents', {}, { reload: true });
          });
        }
      });
    }

  }

  return ceventViewDirective;
});
