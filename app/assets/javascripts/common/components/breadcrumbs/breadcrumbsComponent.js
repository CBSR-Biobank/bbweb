/**
 * AngularJS component available to the rest of the application.
 *
 * @namespace common.components.breadcrumbs
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * An empty controller for now.
 */
class BreadcrumbsController {
}

/**
 * An AngularJS component that displays breadcrumbs at the top of a page.
 *
 * @memberOf common.components.breadcrumbs
 *
 * @param {Array<common.services.BreadcrumbService.breadcrumb>} crumbs - The breadcrumbs to display.
 */
const breadcrumbsComponent = {
  template: require('./breadcrumbs.html'),
  controller: BreadcrumbsController,
  controllerAs: 'vm',
  bindings: {
      crumbs: '<'
  }
};

export default ngModule => ngModule.component('breadcrumbs', breadcrumbsComponent)
