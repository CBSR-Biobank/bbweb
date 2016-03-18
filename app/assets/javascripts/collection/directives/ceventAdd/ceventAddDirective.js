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
    vm.timeCompleted   = { date: null, time: null };

    vm.submit = submit;
    vm.cancel = cancel;

    // for date picker
    vm.opened = false;
    vm.format = bbwebConfig.datepickerFormat;
    vm.datePicker = {
      options: {
        startingDay: 0
      },
      open: openDatePicker
    };

    // --

    function openDatePicker($event) {
      $event.preventDefault();
      $event.stopPropagation();
      vm.opened = true;
    }

    function submit() {
      vm.collectionEvent.timeCompleted = timeService.dateAndTimeToUtcString(vm.timeCompleted.date,
                                                                            vm.timeCompleted.time);
      vm.collectionEvent.add()
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityService.updateErrorModal(
            error, 'collectionEvent').catch(function () {
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
  }

  return ceventAddDirective;
});
