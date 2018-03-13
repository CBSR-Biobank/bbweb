/**
 * AngularJS Component for {@link domain.participants.Specimen Specimen} collection.
 *
 * @namespace collection.components.participantAdd
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

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

/**
 * An AngularJS component that lets the user add a {@link domain.participants.Participant Participant}.
 *
 * @memberOf collection.components.participantAdd
 *
 * @param {domain.studies.Study} study - the study the *Participant* should be added to.
 *
 * @param {string} uniqueId - the *Unique ID* to give this participant.
 */
const participantAddComponent = {
  template: require('./participantAdd.html'),
  controller: ParticipantAddController,
  controllerAs: 'vm',
  bindings: {
    study:    '<',
    uniqueId: '@'
  }
};

export default ngModule => ngModule.component('participantAdd', participantAddComponent)
