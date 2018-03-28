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
              userStateLabelService) {
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
                    userStateLabelService
                  });
  }

  $onInit() {
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.access'),
      this.breadcrumbService.forState('home.admin.access.users'),
      this.breadcrumbService.forStateWithFunc(
        'home.admin.access.users.user',
        () => this.gettextCatalog.getString('{{name}}', { name: this.user.name }))
    ];

    this.studyMemberships     = '';
    this.centreMemberships    = '';
    this.allowRemoveAvatarUrl = (this.user.avatarUrl !== null);
    this.stateLabelFunc       = this.userStateLabelService.stateToLabelFunc(this.user.state);


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
    if (err.data) {
      this.notificationsService.updateError(
        this.gettextCatalog.getString('Your change could not be saved: ') + err.data.message,
        this.gettextCatalog.getString('Cannot apply your change'));
    }
  }

  postUpdate(message, title, timeout) {
    timeout = timeout || 1500;
    return user => {
      this.user = user;
      this.allowRemoveAvatarUrl = (this.user.avatarUrl !== null);
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
      .catch(this.updateError.bind(this));
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
      .catch(this.updateError.bind(this));
  }

  updateAvatarUrl() {
    this.modalInput.url(this.gettextCatalog.getString('Update avatar URL'),
                   this.gettextCatalog.getString('Avatar URL'),
                   this.user.avatarUrl)
      .result
      .then(avatarUrl => this.user.updateAvatarUrl(avatarUrl))
      .then(this.postUpdate(this.gettextCatalog.getString('Avatar URL updated successfully.'),
                            this.gettextCatalog.getString('Update successful')))
      .catch(this.updateError.bind(this));
  }

  removeAvatarUrl() {
    const header = this.gettextCatalog.getString('Remove Avatar URL'),
          body = this.gettextCatalog.getString('Are you sure you want to remove your Avatar URL?');
    this.modalService.modalOkCancel(header, body)
      .then(() => this.user.updateAvatarUrl(null))
      .then(this.postUpdate(this.gettextCatalog.getString('Avatar URL remove successfully.'),
                            this.gettextCatalog.getString('Remove successful')))
      .catch(this.updateError.bind(this));
  }

  updatePassword() {
    this.modalInput.password(this.gettextCatalog.getString('Change password')).result
      .then(result => this.user.updatePassword(result.currentPassword, result.newPassword))
      .then(this.postUpdate(this.gettextCatalog.getString('Your password was updated successfully.'),
                            this.gettextCatalog.getString('Update successful')))
      .catch(err => {
        if (err.data.message.indexOf('invalid password') > -1) {
          this.notificationsService.error(
            this.gettextCatalog.getString('Your current password was incorrect.'),
            this.gettextCatalog.getString('Cannot update your password'));
        } else {
          this.updateError(err);
        }
      });
  }

  addRole(roleId) {
    this.user.addRole(roleId)
      .then(user => {
        this.user = user
      })
  }

  removeRole(roleName) {
    this.user.removeRole(roleName.id)
      .then(user => {
        this.user = user;
      })
  }

  addMembership(membershipId) {
    this.user.addMembership(membershipId)
      .then(user => {
        this.user = user
      })
  }

  removeMembership(membershipName) {
    this.user.removeMembership(membershipName.id)
      .then(user => {
        this.user = user;
      })
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

export default ngModule => ngModule.component('userProfile', userProfileComponent)
