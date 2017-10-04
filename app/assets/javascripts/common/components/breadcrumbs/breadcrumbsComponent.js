
class BreadcrumbsController {

}

//BreadcrumbsController.$inject = [];

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

export default component;
