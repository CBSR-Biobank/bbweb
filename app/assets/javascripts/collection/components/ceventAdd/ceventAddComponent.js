/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Used to add a collection event.
   */
  var component = {
    template: require('./ceventAdd.html'),
    controller: CeventAddController,
    controllerAs: 'vm',
    bindings: {
      study:               '<',
      participant:         '<',
      collectionEventType: '<'
    }
  };

  CeventAddController.$inject = [
    '$state',
    'gettextCatalog',
    'notificationsService',
    'domainNotificationService',
    'timeService',
    'CollectionEvent',
    'breadcrumbService'
  ];

  /*
   * Controller for this component.
   */
  function CeventAddController($state,
                               gettextCatalog,
                               notificationsService,
                               domainNotificationService,
                               timeService,
                               CollectionEvent,
                               breadcrumbService) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.collectionEvent = new CollectionEvent({ participantId: vm.participant.id },
                                               vm.collectionEventType);

      vm.title = gettextCatalog.getString(
        'Participant {{id}}: Add collection event', { id: vm.participant.uniqueId });
      vm.timeCompleted = new Date();

      vm.submit = submit;
      vm.cancel = cancel;
      vm.dateTimeOnEdit = dateTimeOnEdit;

      vm.breadcrumbs = [
        breadcrumbService.forState('home'),
        breadcrumbService.forState('home.collection'),
        breadcrumbService.forStateWithFunc('home.collection.study', function () {
          return vm.study.name;
        }),
        breadcrumbService.forStateWithFunc(
          'home.collection.study.participant.cevents',
          function () {
            return gettextCatalog.getString('Participant {{uniqueId}}',
                                            { uniqueId: vm.participant.uniqueId });
          }),
        breadcrumbService.forStateWithFunc(
          'home.collection.study.participant.cevents.add',
          function () {
            return gettextCatalog.getString('Event type: {{type}}',
                                            { type: vm.collectionEventType.name });
          })
      ];

    }

    function submit() {
      vm.collectionEvent.timeCompleted = timeService.dateAndTimeToUtcString(vm.timeCompleted);
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

  return component;
});
