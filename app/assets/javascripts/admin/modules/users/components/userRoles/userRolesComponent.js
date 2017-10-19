/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /*
   * Allows the logged in user to modify another user's roles.
   */
  var component = {
    template: require('./userRoles.html'),
    controller: UserRolesController,
    controllerAs: 'vm',
    bindings: {
      user: '<'
    }
  };

  /*
   * Controller for this component.
   */
  /* @ngInject */
  function UserRolesController(breadcrumbService) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.breadcrumbs = [
        breadcrumbService.forState('home'),
        breadcrumbService.forState('home.admin'),
        breadcrumbService.forState('home.admin.users'),
        breadcrumbService.forState('home.admin.users.roles'),
      ];
    }

  }

  return component;
});
