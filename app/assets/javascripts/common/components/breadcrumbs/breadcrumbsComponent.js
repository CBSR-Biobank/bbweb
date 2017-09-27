
class BreadcrumbsController {

}

//BreadcrumbsController.$inject = [];

/**
 *
 */
var component = {
  template: require('./breadcrumbs.html'),
  controller: BreadcrumbsController,
  controllerAs: 'vm',
  bindings: {
      crumbs: '<'
  }
};

export default component;
