/**
 * AngularJS Component used in the home page.
 *
 * @namespace home.components.about
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
class AboutController {

  constructor(breadcrumbService) {
    'ngInject';
    Object.assign(this, { breadcrumbService });
  }

  $onInit() {
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.about')
    ];
  }
}

/**
 * An AngularJS component for the *About* page.
 *
 * @memberOf home.components.about
 */
const aboutComponent = {
  template: require('./about.html'),
  controller: AboutController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('about', aboutComponent)
