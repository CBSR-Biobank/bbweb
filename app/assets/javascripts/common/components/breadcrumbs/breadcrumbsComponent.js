/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/*
 * An empty controller for now.
 */
class BreadcrumbsController {
}

/**
 * @class breadcrumbs
 *
 * An AngularJS component that displays breadcrumbs at the top of a page.
 *
 * @memberOf ng.common.components
 *
 * @param {ng.common.services.BreadcrumbService.breadcrumb} crumbs - The breadcrumbs to display.
 */
const breadcrumbs = {
  template: require('./breadcrumbs.html'),
  controller: BreadcrumbsController,
  controllerAs: 'vm',
  bindings: {
      crumbs: '<'
  }
};

export default ngModule => ngModule.component('breadcrumbs', breadcrumbs)
