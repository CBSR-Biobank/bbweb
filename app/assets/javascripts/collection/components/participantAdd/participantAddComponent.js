/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/**
 * This component is used for adding a participant.
 */
var component = {
  template: require('./participantAdd.html'),
  controller: ParticipantAddController,
  controllerAs: 'vm',
  bindings: {
    study:    '<',
    uniqueId: '@'
  }
};

/*
 * Controller for this component.
 */
/* @ngInject */
function ParticipantAddController($state,
                                  gettextCatalog,
                                  Participant,
                                  domainNotificationService,
                                  notificationsService,
                                  breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.collection'),
      breadcrumbService.forStateWithFunc('home.collection.study', function () {
        return vm.study.name;
      }),
      breadcrumbService.forState('home.collection.study.participantAdd')
    ];

    vm.participant = new Participant({ uniqueId: vm.uniqueId }, vm.study);
    vm.submit      = submit;
    vm.cancel      = cancel;
  }

  function submit(participant) {
    // convert the data from the form to data expected by REST API
    participant.add()
      .then(submitSuccess)
      .catch(function(error) {
        return domainNotificationService.updateErrorModal(error, gettextCatalog.getString('participant'));
      }).catch(function () {
        $state.go('home.collection.study', { studyId: vm.study.id });
      });
  }

  function submitSuccess(reply) {
    // the reply contains the id assigned to this new participant, therefore, the state data can be updated
    notificationsService.submitSuccess();
    $state.go(
      'home.collection.study.participant.summary',
      { studyId: vm.study.id, participantId: reply.id },
      { reload: true });
  }

  function cancel() {
    $state.go('home.collection.study', { studyId: vm.study.id });
  }
}

export default ngModule => ngModule.component('participantAdd', component)
