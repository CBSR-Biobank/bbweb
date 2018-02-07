/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Controller for this component.
 */
/* @ngInject */
class Controller {

  constructor($log,
              breadcrumbService,
              gettextCatalog,
              domainNotificationService,
              RoleName,
              asyncInputModal) {
    'ngInject'
    Object.assign(this, {
      $log,
      breadcrumbService,
      domainNotificationService,
      gettextCatalog,
      RoleName,
      asyncInputModal
    })
  }

  $onInit() {
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.access'),
      this.breadcrumbService.forState('home.admin.access.users'),
      this.breadcrumbService.forStateWithFunc(
        'home.admin.access.users.user',
        () => this.gettextCatalog.getString('{{name}}', { name: this.user.name })),
      this.breadcrumbService.forState('home.admin.access.users.user.roles')
    ];

    this.updateLabels();
  }

  addRole() {
    const rolesToOmit = this.user.roles

    const getMatchingRoleNames = (viewValue) =>
          this.RoleName.list({ filter: 'name:like:' + viewValue}, rolesToOmit)
          .then(nameObjs =>
                nameObjs.map((nameObj) => ({ label: nameObj.name, obj: nameObj })))

    this.asyncInputModal.open(this.gettextCatalog.getString('Add a role'),
                              this.gettextCatalog.getString('Role'),
                              this.gettextCatalog.getString('enter a role\'s name or partial name'),
                              this.gettextCatalog.getString('No matching roles found'),
                              getMatchingRoleNames).result
      .then((modalValue) => {
        this.user.addRole(modalValue.obj.id).then((user) => {
          this.user = user
          this.roleNameLabels = this.entityNamesToLabels(this.user.roleData)
        })
      })
      .catch((error) => {
        this.$log.error(error);
      })
  }

  roleLabelSelected(roleName) {
    const promiseFn = () =>
          this.user.removeRole(roleName.id).then((user) => {
            this.user =  user;
            this.notificationsService.success(this.gettextCatalog.getString(
              'Role {{name}} removed',
              { name: roleName.name }))
          })

    this.domainNotificationService.removeEntity(
      promiseFn,
      this.gettextCatalog.getString('Remove role from user'),
      this.gettextCatalog.getString(
        'Are you sure you want to remove the role named <strong>{{name}}</strong> from this user?',
        { name: roleName.name }),
      this.gettextCatalog.getString('Remove failed'),
      this.gettextCatalog.getString('The role named {{name}} cannot be removed', { name: roleName.name }))
  }

  copyRoles() {
  }

  entityNamesToLabels(entityData) {
    const labels = entityData.map((roleInfo) => ({
      label:   roleInfo.name,
      tooltip: this.gettextCatalog.getString('Remove ' + roleInfo.name),
      obj:     roleInfo
    }))
    return _.sortBy(labels, [ 'label' ])
  }

  updateLabels() {
    this.roleNameLabels = this.entityNamesToLabels(this.user.roleData)
  }

}

/*
 * Allows the logged in user to modify another user's roles.
 */
var component = {
  template: require('./userRoles.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
    user: '<'
  }
};

export default ngModule => ngModule.component('userRoles', component)
