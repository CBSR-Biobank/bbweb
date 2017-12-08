/**
 *
 */
import _       from 'lodash'
import angular from 'angular'

class RoleViewController {

  constructor($state,
              notificationsService,
              domainNotificationService,
              gettextCatalog,
              breadcrumbService,
              userService,
              modalInput,
              asyncInputModal,
              EntityInfo,
              UserName,
              RoleName,
              accessItemNameFactory) {
    'ngInject'

    Object.assign(this,
                  {
                    $state,
                    notificationsService,
                    domainNotificationService,
                    gettextCatalog,
                    breadcrumbService,
                    userService,
                    modalInput,
                    asyncInputModal,
                    EntityInfo,
                    UserName,
                    RoleName,
                    accessItemNameFactory
                  })
  }

  $onInit() {
    this.userCanUpdate = this.userService.getCurrentUser().hasUserAdminRole()

    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.users'),
      this.breadcrumbService.forState('home.admin.users.roles'),
      this.breadcrumbService.forStateWithFunc(
        'home.admin.users.roles.role',
        () => this.role.name)
    ]

    this.roleHasUsers       = (this.role.userData.length > 0)
    this.roleHasParentRoles = (this.role.parentData.length > 0)
    this.roleHasChildren    = (this.role.childData.length > 0)
    this.updateLabels(this.role)
  }

  entityNamesToLabels(entityData) {
    var labels = entityData.map((userInfo) => ({
      label:   userInfo.name,
      tooltip: this.gettextCatalog.getString('Remove ' + userInfo.name),
      obj:     userInfo
    }))
    return _.sortBy(labels, [ 'label' ])
  }

  remove() {
    const promiseFn =
          () => this.role.remove()
          .then(() => {
            this.notificationsService.success(this.gettextCatalog.getString('Role removed'))
            this.$state.go('home.admin.users.roles', {}, { reload: true })
          })

    this.domainNotificationService.removeEntity(
      promiseFn,
      this.gettextCatalog.getString('Remove role'),
      this.gettextCatalog.getString('Are you sure you want to remove the role named <b>{{name}}</b>?',
                                    { name: this.role.name }),
      this.gettextCatalog.getString('Remove failed'),
      this.gettextCatalog.getString('Role with name {{name}} cannot be removed',
                                    { name: this.role.name }))
  }

  postUpdate(message, title, timeout = 1500) {
    return (role) => {
      this.role = role
      this.notificationsService.success(message, title, timeout)
    }
  }

  editName() {
    this.modalInput.text(this.gettextCatalog.getString('Role name'),
                         this.gettextCatalog.getString('Name'),
                         this.role.name,
                         { required: true, minLength: 2 }).result
      .then((name) => {
        this.role.updateName(name)
          .then(this.postUpdate(this.gettextCatalog.getString('Name changed successfully.'),
                                this.gettextCatalog.getString('Change successful')))
          .catch(this.notificationsService.updateError)
      })
      .catch(angular.noop)
  }

  editDescription() {
    this.modalInput.textArea(this.gettextCatalog.getString('Role description'),
                             this.gettextCatalog.getString('Description'),
                             this.role.description).result
      .then((description) => {
        this.role.updateDescription(description)
          .then(this.postUpdate(this.gettextCatalog.getString('Description changed successfully.'),
                                this.gettextCatalog.getString('Change successful')))
          .catch(this.notificationsService.updateError)
      })
      .catch(angular.noop)
  }

  addUser() {
    const getMatchingUserNames = viewValue =>
          this.UserName.list({ filter: 'name:like:' + viewValue}, this.role.userData)
          .then((nameObjs) => nameObjs.map((nameObj) => ({ label: nameObj.name, obj: nameObj })))

    this.asyncInputModal.open(this.gettextCatalog.getString('Add user to role'),
                              this.gettextCatalog.getString('User'),
                              this.gettextCatalog.getString('enter a user\'s name or partial name'),
                              this.gettextCatalog.getString('No matching users found'),
                              getMatchingUserNames).result
      .then((modalValue) => {
        this.role.addUser(modalValue.obj.id).then((role) => {
          this.role = role
          this.userNameLabels = this.entityNamesToLabels(this.role.userData)
        })
      })
      .catch(angular.noop)
  }

  // this method is invoked by a child component, so a callback function is returned
  // called to remove a user from the role
  userLabelSelected(userName) {
    const promiseFn = () => this.role.removeUser(userName.id)
          .then(role => {
            this.notificationsService.success(
              this.gettextCatalog.getString('User {{name}} removed', { name: userName.name }))
            this.updateRole(role)
          })

    this.domainNotificationService.removeEntity(
      promiseFn,
      this.gettextCatalog.getString('Remove user from role'),
      this.gettextCatalog.getString(
        'Are you sure you want to remove the user named <strong>{{name}}</strong> from this role?',
        { name: userName.name }),
      this.gettextCatalog.getString('Remove failed'),
      this.gettextCatalog.getString(
        'User named {{name}} cannot be removed',
        { name: userName.name }))
  }

  addParentRole() {
    const parentRolesToOmit =
          this.role.parentData
          .concat(this.role.childData)
          .concat(this.EntityInfo.create({ id: this.role.id, name: this.role.name }))

    const getMatchingRoleNames = (viewValue) =>
          this.RoleName.list({ filter: 'name:like:' + viewValue}, parentRolesToOmit)
          .then(nameObjs =>
                nameObjs.map((nameObj) => ({ label: nameObj.name, obj: nameObj })))

    this.asyncInputModal.open(this.gettextCatalog.getString('Add parent role to role'),
                              this.gettextCatalog.getString('Parent role'),
                              this.gettextCatalog.getString('enter a parent role\'s name or partial name'),
                              this.gettextCatalog.getString('No matching parent roles found'),
                              getMatchingRoleNames).result
      .then((modalValue) => {
        this.role.addParentRole(modalValue.obj.id).then((role) => {
          this.role = role
          this.parentNameLabels = this.entityNamesToLabels(this.role.parentData)
        })
      })
      .catch(angular.noop)
  }

  // this method is invoked by a child component, so a callback function is returned
  // called to remove a user from the role
  parentRoleLabelSelected(parentRoleName) {
    const promiseFn = () =>
          this.role.removeParentRole(parentRoleName.id).then((role) => {
            this.notificationsService.success(this.gettextCatalog.getString(
              'ParentRole {{name}} removed',
              { name: parentRoleName.name }))
            this.updateRole(role)
          })

    this.domainNotificationService.removeEntity(
      promiseFn,
      this.gettextCatalog.getString('Remove parent role from role'),
      this.gettextCatalog.getString(
        'Are you sure you want to remove the parent role named <strong>{{name}}</strong> from this role?',
        { name: parentRoleName.name }),
      this.gettextCatalog.getString('Remove failed'),
      this.gettextCatalog.getString(
        'Parent role named {{name}} cannot be removed',
        { name: parentRoleName.name }))
  }

  addChildRole() {
    const childRolesToOmit =
          this.role.childData
          .concat(this.role.parentData)
          .concat(this.EntityInfo.create({ id: this.role.id, name: this.role.name }))

    const getMatchingAccessItemNames = (viewValue) =>
          this.accessItemNameFactory.list({ filter: 'name:like:' + viewValue}, childRolesToOmit)
          .then(nameObjs =>
                nameObjs.map((nameObj) => ({ label: nameObj.name, obj: nameObj })))

    this.asyncInputModal.open(this.gettextCatalog.getString('Add child role to role'),
                              this.gettextCatalog.getString('Child role'),
                              this.gettextCatalog.getString('enter a child role\'s name or partial name'),
                              this.gettextCatalog.getString('No matching child roles found'),
                              getMatchingAccessItemNames).result
      .then((modalValue) => {
        this.role.addChildRole(modalValue.obj.id).then((role) => {
          this.role = role
          this.childNameLabels = this.entityNamesToLabels(this.role.childData)
        })
      })
      .catch(angular.noop)
  }

  // this method is invoked by a child component, so a callback function is returned
  // called to remove a user from the role
  childRoleLabelSelected(childRoleName) {
    const promiseFn = () =>
          this.role.removeChildRole(childRoleName.id).then((role) => {
            this.notificationsService.success(this.gettextCatalog.getString(
              'ChildRole {{name}} removed',
              { name: childRoleName.name }))
            this.updateRole(role)
          })

    this.domainNotificationService.removeEntity(
      promiseFn,
      this.gettextCatalog.getString('Remove child role from role'),
      this.gettextCatalog.getString(
        'Are you sure you want to remove the child role named <strong>{{name}}</strong> from this role?',
        { name: childRoleName.name }),
      this.gettextCatalog.getString('Remove failed'),
      this.gettextCatalog.getString(
        'Child role named {{name}} cannot be removed',
        { name: childRoleName.name }))
  }

  back() {
    this.$state.go('home.admin.users.roles')
  }

  updateLabels(role) {
    this.userNameLabels   = this.entityNamesToLabels(role.userData)
    this.parentNameLabels = this.entityNamesToLabels(role.parentData)
    this.childNameLabels  = this.entityNamesToLabels(role.childData)
  }

  updateRole(role) {
    this.role = role
    this.updateLabels(this.role)
  }

}

var component = {
  template: require('./roleView.html'),
  controller: RoleViewController,
  controllerAs: 'vm',
  bindings: {
    role: '<'
  }
}

export default ngModule => ngModule.component('roleView', component)
