/**
 * AngularJS Component for {@link domain.centres.Centre Centre} administration.
 *
 * @namespace admin.centres.components.centresAdmin
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
/* @ngInject */
function CentresAdminController(breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //---

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.centres')
    ];
  }
}

/**
 * An AngularJS component for the {@link domain.centres.Centre Centre} administration main page.
 *
 * @memberOf admin.centres.components.centresAdmin
 */
const centresAdminComponent = {
  template: require('./centresAdmin.html'),
  controller: CentresAdminController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('centresAdmin', centresAdminComponent)
