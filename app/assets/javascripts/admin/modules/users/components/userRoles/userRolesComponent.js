/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
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
    this.matchingRoleNames.open(this.gettextCatalog.getString('Add a role'),
                                this.gettextCatalog.getString('Role'),
                                this.gettextCatalog.getString('enter a role\'s name or partial name'),
                                this.gettextCatalog.getString('No matching roles found'),
                                this.user.roles)
      .then((modalValue) => {
        this.onRoleAddRequest()(modalValue.obj.id)
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

/*
 * Allows the logged in user to modify another user's roles.
 */
var userRoles = {
  template: require('./userRoles.html'),
  controller: UserRolesController,
  controllerAs: 'vm',
  bindings: {
    user:                '<',
    onRoleAddRequest:    '&',
    onRoleRemoveRequest: '&'
  }
};

export default ngModule => ngModule.component('userRoles', userRoles)
