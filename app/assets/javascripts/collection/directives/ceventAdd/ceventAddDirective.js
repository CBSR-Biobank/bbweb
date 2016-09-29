/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  /**
   *
   */
  function ceventAddDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study: '=',
        participant: '=',
        collectionEventType: '='
      },
      templateUrl : '/assets/javascripts/collection/directives/ceventAdd/ceventAdd.html',
      controller: CollectionAddCtrl,
      controllerAs: 'vm'
    };
    return directive;
  }

  CollectionAddCtrl.$inject = [
    '$state',
    'gettextCatalog',
    'bbwebConfig',
    'notificationsService',
    'domainNotificationService',
    'timeService',
    'CollectionEvent'
  ];

  /**
   * Used to add or edit a collection event.
   */
  function CollectionAddCtrl($state,
                             gettextCatalog,
                             bbwebConfig,
                             notificationsService,
                             domainNotificationService,
                             timeService,
                             CollectionEvent) {
    var vm = this;

    vm.collectionEvent = new CollectionEvent({ participantId: vm.participant.id },
                                             vm.collectionEventType);

    vm.title = gettextCatalog.getString(
      'Participant {{id}}: Add collection event', { id: vm.participant.uniqueId });
    vm.timeCompleted = new Date();

    vm.submit = submit;
    vm.cancel = cancel;
    vm.dateTimeOnEdit = dateTimeOnEdit;

    // --

    function submit() {
      vm.collectionEvent.timeCompleted = timeService.dateToUtcString(vm.timeCompleted);
      vm.collectionEvent.add()
        .then(submitSuccess)
        .catch(submitError);

      function submitSuccess(cevent) {
        notificationsService.submitSuccess();
        $state.go('home.collection.study.participant.cevents.details',
                  { collectionEventId: cevent.id },
                  { reload: true });
      }

      function submitError(error) {
        domainNotificationService.updateErrorModal(error, gettextCatalog.getString('collectionEvent'))
          .catch(function () {
            $state.go('home.collection.study.participant', { participantId: vm.participant.id });
          });
      }
    }

    function cancel() {
      $state.go('home.collection.study.participant.cevents');
    }

    function dateTimeOnEdit(datetime) {
      vm.timeCompleted = datetime;
    }
  }

  return ceventAddDirective;
});
