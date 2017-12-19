/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: require('./participantGet.html'),
  controller: ParticipantGetController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

const patientDoesNotExistRe = /EntityCriteriaNotFound: participant with unique ID does not exist/,
      studyMismatchRe = /EntityCriteriaError: participant not in study/i;

/*
 * Controller for this component.
 */
/* @ngInject */
function ParticipantGetController($q,
                                  $log,
                                  $state,
                                  gettextCatalog,
                                  modalService,
                                  Participant,
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
      })
    ];

    vm.uniqueId = '';
    vm.onSubmit = onSubmit;
  }

  function onSubmit() {
    if (vm.uniqueId.length > 0) {
      Participant.getByUniqueId(vm.study.id, vm.uniqueId)
        .then(function (participant) {
          $state.go('home.collection.study.participant.summary', { slug: participant.slug });
        })
        .catch(participantGetError);
    }
  }

  function participantGetError(error) {
    if (error.status !== 'error') {
      $log.error('expected an error reply: ', JSON.stringify(error));
      return;
    }

    if (error.message.match(patientDoesNotExistRe)) {
      createParticipantModal(vm.uniqueId);
    } else if (error.message.match(studyMismatchRe)) {
      modalService.modalOk(
        gettextCatalog.getString('Duplicate unique ID'),
        gettextCatalog.getString(
          'Unique ID <strong>{{id}}</strong> is already in use by a participant ' +
            'in another study. Please use a different one.',
          { id: vm.uniqueId }))
        .then(function () {
          vm.uniqueId = '';
        });
    } else {
      $log.error('could not get participant by uniqueId: ', JSON.stringify(error));
    }
  }

  function createParticipantModal(uniqueId) {
    modalService.modalOkCancel(
      gettextCatalog.getString('Create participant'),
      gettextCatalog.getString(
        'Would you like to create participant with unique ID <strong>{{id}}</strong>?',
        { id: uniqueId })
    ).then(function() {
      $state.go('home.collection.study.participantAdd', { uniqueId: uniqueId });
    }).catch(function() {
      $state.reload();
    });
  }
}

export default ngModule => ngModule.component('participantGet', component)
