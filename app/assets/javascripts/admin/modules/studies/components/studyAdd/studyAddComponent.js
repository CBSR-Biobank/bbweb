/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: require('./studyAdd.html'),
  controller: StudyAddController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

const returnState = 'home.admin.studies';

/*
 * Controller for this component.
 */
/* @ngInject */
function StudyAddController($state,
                            gettextCatalog,
                            notificationsService,
                            domainNotificationService,
                            breadcrumbService) {

  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.studies'),
      breadcrumbService.forState('home.admin.studies.add')
    ];

    vm.submit = submit;
    vm.cancel = cancel;
  }

  function submit(study) {
    study.add()
      .then(submitSuccess)
      .catch(submitError);

    function submitSuccess() {
      notificationsService.submitSuccess();
      $state.go(returnState, {}, { reload: true });
    }

    function submitError(error) {
      domainNotificationService.updateErrorModal(error, gettextCatalog.getString('study'));
    }
  }

  function cancel() {
    $state.go(returnState);
  }
}

export default ngModule => ngModule.component('studyAdd', component)
