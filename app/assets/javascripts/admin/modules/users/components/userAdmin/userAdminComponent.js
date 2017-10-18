/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: require('./userAdmin.html'),
  controller: UserAdminController,
  controllerAs: 'vm'
};

/*
 * Shows a table of users.
 */
/* @ngInject */
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

export default ngModule => ngModule.component('userAdmin', component)
