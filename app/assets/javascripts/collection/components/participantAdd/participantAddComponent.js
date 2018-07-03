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
      .then(reply => {
        // the reply contains the id assigned to this new participant
        //
        // therefore, the state data can be updated
        notificationsService.submitSuccess();
        $state.go(
          'home.collection.study.participant.summary',
          {
            studySlug:       vm.study.slug,
            participantSlug: reply.slug
          },
          { reload: true });
      })
      .catch(error =>
             domainNotificationService.updateErrorModal(error, gettextCatalog.getString('participant')))
      .catch(() => {
        $state.go('home.collection.study', { studyId: vm.study.id });
      });
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

function resolveParticipantUniqueId($transition$) {
  'ngInject';
  return $transition$.params().uniqueId;
}

function stateConfig($stateProvider, $urlRouterProvider) {
  'ngInject';
  $stateProvider.state('home.collection.study.participantAdd', {
    url: '/participants/add/{uniqueId}',
    resolve: {
      uniqueId: resolveParticipantUniqueId
    },
    views: {
      'main@': 'participantAdd'
    }
  });
  $urlRouterProvider.otherwise('/');
}

export default ngModule => {
  ngModule
    .config(stateConfig)
    .component('participantAdd', participantAddComponent);
}
