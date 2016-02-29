/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  UserProfileCtrl.$inject = [
    'modalService',
    'notificationsService',
    'User',
    'user'
  ];

  /**
   * Displays a list of users in a table.
   */
  function UserProfileCtrl(modalService,
                           notificationsService,
                           User,
                           user) {
    var vm = this;

    vm.user            = new User(user);
    vm.updateName      = updateName;
    vm.updateEmail     = updateEmail;
    vm.updatePassword  = updatePassword;
    vm.updateAvatarUrl = updateAvatarUrl;
    vm.removeAvatarUrl = removeAvatarUrl;

    //--

    function updateError(err) {
      notificationsService.error(
        'Your change could not be saved: ' + err.data.message,
        'Cannot apply your change');
    }

    function postUpdate(message, title, timeout) {
      return function (user) {
        vm.user = user;
        notificationsService.success(message, title, timeout);
      };
    }

    function updateName() {
      var name = vm.user.name;
      modalService.modalTextInput(
        'Update user name',
        'Name',
        name
      ).then(function (name) {
        vm.user.updateName(name)
          .then(postUpdate('User name updated successfully.',
                           'Update successful',
                           1500))
          .catch(updateError);
      });
    }

    function updateEmail() {
      modalService.modalEmailInput(
        'Update user email',
        'Email',
        vm.user.email
      ).then(function (email) {
        vm.user.updateEmail(email)
          .then(postUpdate('Email updated successfully.',
                           'Update successful',
                           1500))
          .catch(updateError);
      });
    }

    function updateAvatarUrl() {
      modalService.modalUrlInput(
        'Update avatar URL',
        'Avatar URL',
        vm.user.avatarUrl
      ).then(function (avatarUrl) {
        vm.user.updateAvatarUrl(avatarUrl)
          .then(postUpdate('Avatar URL updated successfully.',
                           'Update successful',
                           1500))
          .catch(updateError);
      });
    }

    function removeAvatarUrl() {
      var modalDefaults = {};
      var modalOptions = {
        headerHtml       : 'Remove Avatar URL',
        bodyHtml         : 'Are you sure you want to remove your Avatar URL?',
        closeButtonText  : 'Cancel',
        actionButtonText : 'OK'
      };

      modalService.showModal(modalDefaults, modalOptions)
        .then(function() {
          vm.user.updateAvatarUrl(null)
            .then(postUpdate('Avatar URL remove successfully.',
                             'Remove successful',
                             1500))
            .catch(updateError);
        });
    }

    function updatePassword() {
      modalService.passwordUpdateModal().then(function (result) {
        vm.user.updatePassword(result.currentPassword, result.newPassword)
          .then(postUpdate('Your password was updated successfully.',
                           'Update successful',
                           1500))
          .catch(function (err) {
            if (err.data.message.indexOf('invalid password') > -1) {
              notificationsService.error(
                'Your current password was incorrect.',
                'Cannot update your password');
            } else {
              updateError(err);
            }
          });
      });
    }
  }

  return UserProfileCtrl;
});
