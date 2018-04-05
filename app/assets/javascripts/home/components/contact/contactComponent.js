/**
 * AngularJS Component used in the home page.
 *
 * @namespace home.components.contact
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/*
 * Controller for this component.
 */
class Controller {

  constructor(breadcrumbService) {
    'ngInject';
    Object.assign(this, { breadcrumbService });
  }

  $onInit() {
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.contact')
    ];
  }
}

/**
 * An AngularJS component for the *Contact Us* page.
 *
 * @memberOf home.components.contact
 */
const contactComponent = {
  template: require('./contact.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('contact', contactComponent)
