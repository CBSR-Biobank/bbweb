/**
 * AngularJS Component for {@link domain.studies.Study Study} administration.
 *
 * @namespace admin.studies.components.studiesAdmin
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
/* @ngInject */
function StudiesAdminController(breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.studies')
    ];
  }

}

/**
 * An AngularJS component that displays the main page for {@link domain.studies.Study Study} Adminisrattion.
 *
 * This page lists all studies (in a paged fashion) in the system. The user can then select one of these
 * studies to configure it.
 *
 * @memberOf admin.studies.components.studiesAdmin
 */
const studiesAdminComponent = {
  template: require('./studiesAdmin.html'),
  controller: StudiesAdminController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('studiesAdmin', studiesAdminComponent)
