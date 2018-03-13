/**
 * AngularJS Component for {@link domain.users.User User} administration.
 *
 * @namespace admin.users.components.userRoles
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Controller for this component.
 */
class UserRolesController {

  constructor($q,
              $log,
              asyncInputModal,
              gettextCatalog,
              domainNotificationService,
              notificationsService,
              modalService,
              RoleName,
              UserName,
              matchingRoleNames,
              matchingUserNames) {
    'ngInject'
    Object.assign(
      this,
      {
        $q,
        $log,
        asyncInputModal,
        gettextCatalog,
        domainNotificationService,
        notificationsService,
        modalService,
        RoleName,
        UserName,
        matchingRoleNames,
        matchingUserNames
      })
  }

  $onInit() {
    this.updateLabels();
  }

  $onChanges(changed) {
    if (changed.user) {
      this.user = changed.user.currentValue;
      this.updateLabels();
    }
  }

  updateLabels() {
    this.roleNameLabels = this.rolesToLabels(this.user.roles)
  }

  rolesToLabels(roles) {
    const labels = roles.map((role) => ({
      label:   role.name,
      tooltip: this.gettextCatalog.getString('Remove ' + role.name),
      obj:     role
    }))
    return _.sortBy(labels, [ 'label' ])
  }

  addRole() {
    let roleLabel;
    this.matchingRoleNames.open(this.gettextCatalog.getString('Add a role'),
                                this.gettextCatalog.getString('Role'),
                                this.gettextCatalog.getString('enter a role\'s name or partial name'),
                                this.gettextCatalog.getString('No matching roles found'),
                                this.user.roles)
      .then((modalValue) => {
        roleLabel = modalValue.label;
        return this.onRoleAddRequest()(modalValue.obj.id)
      })
      .then(() => {
        this.notificationsService.success(
          this.gettextCatalog.getString('Role added: {{name}}', { name: roleLabel }))
      })
      .catch((error) => {
        this.$log.error(error);
      });
  }

  roleLabelSelected(roleName) {
    const promiseFn = () => {
      this.onRoleRemoveRequest()(roleName)
        .then(() => {
          this.notificationsService.success(
            this.gettextCatalog.getString('Role removed: {{name}}', { name: roleName.name }))
        })
       .catch((error) => {
          this.$log.error(error);
        });
    }

    this.domainNotificationService.removeEntity(
      promiseFn,
      this.gettextCatalog.getString('Remove role from user'),
      this.gettextCatalog.getString(
        'Are you sure you want to remove the role named <strong>{{name}}</strong> from this user?',
        { name: roleName.name }),
      this.gettextCatalog.getString('Remove failed'),
      this.gettextCatalog.getString('The role named {{name}} cannot be removed', { name: roleName.name }))

  }
}

/**
 * An AngularJS component that allows the logged in user to modify another {@link domain.users.user
 * User's} {@link domain.access.Role Roles}.
 *
 * @memberOf admin.users.components.userRoles
 *
 * @param {domain.users.User} user - the user to modify roles for.
 *
 * @param {admin.users.components.userRole.onAddRequest} onAddRequest - the function this compoennt
 * calls to request a new role be added.
 *
 * @param {admin.users.components.userRole.onRemoveRequest} onRemoveRequest - the function this component
 * calls to request removal of a role.
 */
const userRolesComponent = {
  template: require('./userRoles.html'),
  controller: UserRolesController,
  controllerAs: 'vm',
  bindings: {
    user:                '<',
    onRoleAddRequest:    '&',
    onRoleRemoveRequest: '&'
  }
};

/**
 * The callback function called by {@link common.components.dateTimePicker dateTimePicker} when the
 * user enters a new value.
 *
 * @callback admin.users.components.userRoles.onAddRequest
 *
 * @param {domain.access.Role} role - the role to add
 */

/**
 * The callback function called by {@link common.components.dateTimePicker dateTimePicker} when the
 * user enters a new value.
 *
 * @callback admin.users.components.userRoles.onRemoveRequest
 *
 * @param {domain.access.Role} role - the role to remove
 */

export default ngModule => ngModule.component('userRoles', userRolesComponent)
