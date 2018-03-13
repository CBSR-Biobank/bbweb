/**
 * AngularJS Component for {@link domain.studies.Study Study} administration.
 *
 * @namespace admin.studies.components.studyAdd
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

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

/**
 * An AngularJS component that allows the user to add a {@link domain.study.Study Study} to the server.
 *
 * The component displays a form the user must fill in.
 *
 * @memberOf admin.studies.components.studyAdd
 */
const studyAddComponent = {
  template: require('./studyAdd.html'),
  controller: StudyAddController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('studyAdd', studyAddComponent)
