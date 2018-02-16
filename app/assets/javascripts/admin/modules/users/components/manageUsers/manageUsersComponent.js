/**
 *
 */

/*
 * Controller for this component.
 */
class ManageUsersController {

  constructor(breadcrumbService, UserCounts) {
    'ngInject'
    Object.assign(this, {breadcrumbService, UserCounts});
  }

  $onInit() {
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.access'),
      this.breadcrumbService.forState('home.admin.access.users')
    ];

    this.haveUsers = false;

    this.UserCounts.get().then(counts => {
      this.userCounts = counts;
      this.haveUsers  = (this.userCounts.total > 0);
    });
  }
}

const manageUsers = {
  template: require('./manageUsers.html'),
  controller: ManageUsersController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('manageUsers', manageUsers);
