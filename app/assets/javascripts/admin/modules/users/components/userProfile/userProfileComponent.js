/**
 * AngularJS Component for {@link domain.users.User User} administration.
 *
 * @namespace admin.users.components.userProfile
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Controller for this component.
 */
/* @ngInject */
class UserProfileController {

  constructor($state,
              gettextCatalog,
              modalService,
              modalInput,
              notificationsService,
              domainNotificationService,
              userService,
              User,
              breadcrumbService,
              userStateLabelService,
              matchingRoleNames,
              matchingMembershipNames) {
    'ngInject'
    Object.assign(this,
                  {
                    $state,
                    gettextCatalog,
                    modalService,
                    modalInput,
                    notificationsService,
                    domainNotificationService,
                    userService,
                    User,
                    breadcrumbService,
                    userStateLabelService,
                    matchingRoleNames,
                    matchingMembershipNames
                  });
  }

  $onInit() {
    this.loggedInUser = this.userService.getCurrentUser();
    this.userIsSelf = (this.loggedInUser.id === this.user.id);
    this.allowStateChange = this.userIsSelf || this.loggedInUser.hasUserAdminRole();

    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.access'),
      this.breadcrumbService.forState('home.admin.access.users'),
      this.breadcrumbService.forStateWithFunc(
        'home.admin.access.users.user',
        () => this.gettextCatalog.getString('{{name}}', { name: this.user.name }))
    ];

    this.studyMemberships  = '';
    this.centreMemberships = '';
    this.stateLabelFunc    = this.userStateLabelService.stateToLabelFunc(this.user.state);

    if (this.user.membership) {
      if (this.user.membership.isForAllStudies()) {
        this.studyMemberships = this.gettextCatalog.getString('All Studies');
      } else if (this.user.membership.studyData.entityData.length > 0){
        this.studyMembershipLabels = _.sortBy(
          this.user.membership.studyData.entityData.map((entityInfo) => ({
            label: entityInfo.name
          })),
          [ 'label']
        );
      }

      if (this.user.membership.isForAllCentres()) {
        this.centreMemberships = this.gettextCatalog.getString('All Centres');
      } else if (this.user.membership.centreData.entityData.length > 0){
        this.centreMembershipLabels = _.sortBy(
          this.user.membership.centreData.entityData.map((entityInfo) => ({
            label: entityInfo.name
          })),
          [ 'label' ]
        );
      }
    } else {
      this.studyMemberships = this.gettextCatalog.getString('None');
      this.centreMemberships = this.gettextCatalog.getString('None');
    }
  }

  updateError(err) {
    if ((typeof err === 'object') && err.message) {
      this.notificationsService.updateError(
        this.gettextCatalog.getString('Your change could not be saved: ') + err.message,
        this.gettextCatalog.getString('Cannot apply your change'));
    }
  }

  postUpdate(message, title, timeout) {
    timeout = timeout || 1500;
    return user => {
      this.user = user;
      this.notificationsService.success(message, title, timeout);
    };
  }

  updateName() {
    const name = this.user.name;

    this.modalInput.text(this.gettextCatalog.getString('Update user name'),
                         this.gettextCatalog.getString('Name'),
                         name,
                         { required: true, minLength: 2 })
      .result
      .then(name => this.user.updateName(name))
      .then(this.postUpdate(this.gettextCatalog.getString('User name updated successfully.'),
                            this.gettextCatalog.getString('Update successful')))
      .catch(err => this.updateError(err));
  }

  updateEmail() {
    this.modalInput.email(this.gettextCatalog.getString('Update user email'),
                     this.gettextCatalog.getString('Email'),
                     this.user.email,
                     { required: true })
      .result
      .then(email => this.user.updateEmail(email))
      .then(this.postUpdate(this.gettextCatalog.getString('Email updated successfully.'),
                            this.gettextCatalog.getString('Update successful')))
      .catch(err => this.updateError(err));
  }

  updateAvatarUrl() {
    this.modalInput.url(this.gettextCatalog.getString('Update avatar URL'),
                   this.gettextCatalog.getString('Avatar URL'),
                   this.user.avatarUrl)
      .result
      .then(avatarUrl => this.user.updateAvatarUrl(avatarUrl))
      .then(this.postUpdate(this.gettextCatalog.getString('Avatar URL updated successfully.'),
                            this.gettextCatalog.getString('Update successful')))
      .catch(err => this.updateError(err));
  }

  removeAvatarUrl() {
    const header = this.gettextCatalog.getString('Remove Avatar URL'),
          body = this.gettextCatalog.getString('Are you sure you want to remove your Avatar URL?');
    this.modalService.modalOkCancel(header, body)
      .then(() => this.user.updateAvatarUrl(null))
      .then(this.postUpdate(this.gettextCatalog.getString('Avatar URL remove successfully.'),
                            this.gettextCatalog.getString('Remove successful')))
      .catch(err => this.updateError(err));
  }

  updatePassword() {
    this.modalInput.password(this.gettextCatalog.getString('Change password')).result
      .then(result => this.user.updatePassword(result.currentPassword, result.newPassword))
      .then(this.postUpdate(this.gettextCatalog.getString('Your password was updated successfully.'),
                            this.gettextCatalog.getString('Update successful')))
      .catch(err => {
        if ((typeof err === 'object') &&
            err.message &&
            (err.message.indexOf('InvalidPassword') > -1)) {
          this.notificationsService.error(
            this.gettextCatalog.getString('Your current password was incorrect.'),
            this.gettextCatalog.getString('Cannot update your password'));
        } else {
          this.updateError(err);
        }
      });
  }

  addRole() {
    let roleLabel;
    this.matchingRoleNames.open(this.gettextCatalog.getString('Add a role'),
                                this.gettextCatalog.getString('Role'),
                                this.gettextCatalog.getString('enter a role\'s name or partial name'),
                                this.gettextCatalog.getString('No matching roles found'),
                                this.user.roles)
      .then(modalValue => {
        roleLabel = modalValue.label;
        return this.user.addRole(modalValue.obj.id)
      })
      .then(user => {
        this.user = user
        this.notificationsService.success(
          this.gettextCatalog.getString('Role added: {{name}}', { name: roleLabel }))
      })
      .catch((error) => {
        this.$log.error(error);
      });
  }

  removeRole(roleName) {
    return this.user.removeRole(roleName.id)
      .then(user => {
        this.user = user;
      })
  }

  addMembership() {
    let membershipLabel;
    this.matchingMembershipNames
      .open(this.gettextCatalog.getString('Add a membership'),
            this.gettextCatalog.getString('Membership'),
            this.gettextCatalog.getString('enter a membership\'s name or partial name'),
            this.gettextCatalog.getString('No matching membership found'),
            this.user.membership)
      .then(modalValue => {
        membershipLabel = modalValue.label;
        return this.user.addMembership(modalValue.obj.id);
      })
      .then(user => {
        this.user = user;
        this.notificationsService.success(
          this.gettextCatalog.getString('Membership added: {{name}}', { name: membershipLabel }))
      })
      .catch((error) => {
        this.$log.error(error);
      });
  }

  removeMembership(membershipName) {
    return this.user.removeMembership(membershipName.id)
      .then(user => {
        this.user = user;
      })
  }

  changeState(stateAction) {
    let stateChangeMsg;

    switch (stateAction) {
    case 'activate':
      stateChangeMsg = this.gettextCatalog.getString('Are you sure you want to activate user {{name}}?',
                                                     { name: this.user.name });
      break;

    case 'lock':
      stateChangeMsg = this.gettextCatalog.getString('Are you sure you want to lock user {{name}}?',
                                                     { name: this.user.name });
      break;

    case 'unlock':
      stateChangeMsg = this.gettextCatalog.getString('Are you sure you want to unlock user {{name}}?',
                                                     { name: this.user.name });
      break;

    default:
      throw new Error('invalid state action: ' + stateAction);
    }

    this.modalService.modalOkCancel(this.gettextCatalog.getString('Confirm state change on user'),
                                    stateChangeMsg)
      .then(
        () => this.user[stateAction]()
      )
      .then(user => {
        this.user = user;
        this.stateLabelFunc = this.userStateLabelService.stateToLabelFunc(this.user.state);
        this.notificationsService.success(this.gettextCatalog.getString('State changed successfully'));
      });
  }

}

/**
 * An AngularJS component that allows the logged in user to modify their or another {@link
 * domain.users.user User's} settings.
 *
 * @memberOf admin.users.components.userProfile
 *
 * @param {domain.users.User} user - the user to modify settings for.
 */
const userProfileComponent = {
  template: require('./userProfile.html'),
  controller: UserProfileController,
  controllerAs: 'vm',
  bindings: {
    user: '<'
  }
};

function resolveUser($transition$, User, resourceErrorService) {
  'ngInject';
  const slug = $transition$.params().slug
  return User.get(slug)
    .catch(resourceErrorService.goto404(`user slug not found: ${slug}`))
}

function stateConfig($stateProvider, $urlRouterProvider) {
  'ngInject';
  $stateProvider.state('home.admin.access.users.user', {
    url: '/:slug',
    resolve: {
      user: resolveUser
    },
    views: {
      'main@': 'userProfile'
    }
  });
  $urlRouterProvider.otherwise('/');
}

export default ngModule => {
  ngModule
    .config(stateConfig)
    .component('userProfile', userProfileComponent)
}
