/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
const component = {
  template: require('./about.html'),
  controller: AboutController,
  controllerAs: 'vm',
  bindings: {
  }
};

/*
 * Controller for this component.
 */
/* @ngInject */
function AboutController(breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.about')
    ];
  }
}

export default ngModule => ngModule.component('about', component)
