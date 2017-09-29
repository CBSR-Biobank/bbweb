/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    template: require('./userAdmin.html'),
    controller: UserAdminController,
    controllerAs: 'vm'
  };

  UserAdminController.$inject = [
    'usersService',
    'UserCounts',
    'breadcrumbService'
  ];

  /*
   * Shows a table of users.
   */
  function UserAdminController(usersService, UserCounts, breadcrumbService) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.breadcrumbs = [
        breadcrumbService.forState('home'),
        breadcrumbService.forState('home.admin'),
        breadcrumbService.forState('home.admin.users')
      ];

      vm.haveUsers = false;

      usersService.requestCurrentUser()
        .then(function (user) {
          vm.user = user;
          return UserCounts.get();
        })
        .then(function (counts) {
          vm.userCounts = counts;
          vm.haveUsers  = (vm.userCounts.total > 0);
        });
    }
  }

  return component;
});
