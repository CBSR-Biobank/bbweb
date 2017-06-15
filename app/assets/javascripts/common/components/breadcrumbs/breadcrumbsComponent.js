/**
 *
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html',
    controller: BreadcrumbsController,
    controllerAs: 'vm',
    bindings: {
      crumbs: '<'
    }
  };

  BreadcrumbsController.$inject = [];

  /*
   * Controller for this component.
   */
  function BreadcrumbsController() {
  }

  return component;
});
