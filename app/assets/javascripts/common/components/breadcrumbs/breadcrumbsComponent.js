/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)

 */
class BreadcrumbsController {
}

/**
 *
 */
const component = {
  template: require('./breadcrumbs.html'),
  controller: BreadcrumbsController,
  controllerAs: 'vm',
  bindings: {
      crumbs: '<'
  }
};

export default ngModule => ngModule.component('breadcrumbs', component)
