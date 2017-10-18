/**
 *
 */

var component = {
  template: require('./manageUsers.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
  }
};

/*
 * Controller for this component.
 */
/* @ngInclude */
function Controller(breadcrumbService,
                    UserCounts) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.users'),
      breadcrumbService.forState('home.admin.users.manage')
    ];

    vm.haveUsers = false;

    UserCounts.get().then(function (counts) {
      vm.userCounts = counts;
      vm.haveUsers  = (vm.userCounts.total > 0);
    });
  }
}

export default ngModule => ngModule.component('manageUsers', component)
