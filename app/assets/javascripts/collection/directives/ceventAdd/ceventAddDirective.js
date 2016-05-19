/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
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
    'bbwebConfig',
    'notificationsService',
    'domainEntityService',
    'timeService',
    'CollectionEvent'
  ];

  /**
   * Used to add or edit a collection event.
   */
  function CollectionAddCtrl($state,
                             bbwebConfig,
                             notificationsService,
                             domainEntityService,
                             timeService,
                             CollectionEvent) {
    var vm = this;

    vm.collectionEvent = new CollectionEvent({ participantId: vm.participant.id },
                                             vm.collectionEventType);

    vm.title = 'Participant ' + vm.participant.uniqueId + ': Add collection event';
    vm.timeCompleted = new Date();
    vm.open = false;
    vm.datetimePickerFormat = bbwebConfig.datepickerFormat;

    vm.submit = submit;
    vm.cancel = cancel;
    vm.openCalendar = openCalendar;

    // --

    function submit() {
      vm.collectionEvent.timeCompleted = timeService.dateToUtcString(vm.timeCompleted);
      vm.collectionEvent.add()
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityService.updateErrorModal(error, 'collectionEvent')
            .catch(function () {
              $state.go('home.collection.study.participant', { participantId: vm.participant.id });
            });
        });
    }

    function submitSuccess(reply) {
      notificationsService.submitSuccess();
      $state.go('home.collection.study.participant.cevents', {}, { reload: true });
    }

    function cancel() {
      $state.go('home.collection.study.participant.cevents');
    }

    function openCalendar(e) {
      vm.open = true;
    }
  }

  return ceventAddDirective;
});
