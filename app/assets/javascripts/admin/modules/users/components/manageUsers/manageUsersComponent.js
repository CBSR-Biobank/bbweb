/**
 * AngularJS Component for {@link domain.users.User User} administration.
 *
 * @namespace admin.users.components.manageUsers
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
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

/**
 * An AngularJS component that allows the logged in user to configure other {@link domain.users.User User's}
 * of the system.
 *
 * @memberOf admin.users.components.manageUsers
 */
const manageUsersComponent = {
  template: require('./manageUsers.html'),
  controller: ManageUsersController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('manageUsers', manageUsersComponent);
