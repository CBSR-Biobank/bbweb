/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/*
 * Controller for this component.
 */
class Controller {

  constructor(breadcrumbService) {
    'ngInject'

    Object.assign(this, { breadcrumbService })
  }

  $onInit() {
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.contact')
    ];
  }
}

const component = {
  template: require('./contact.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('contact', component)
