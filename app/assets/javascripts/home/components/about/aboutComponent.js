/**
 *
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/home/components/about/about.html',
    controller: AboutController,
    controllerAs: 'vm',
    bindings: {
    }
  };

  AboutController.$inject = ['breadcrumbService'];

  /*
   * Controller for this component.
   */
  function AboutController(breadcrumbService) {
    var vm = this;

    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.about')
    ];
  }

  return component;
});
