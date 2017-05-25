/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Allows user to change his / her details, including updating password.
   */
  function userProfileDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {},
      templateUrl : '/assets/javascripts/users/directives/userProfile/userProfile.html',
      controller: UserProfileCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  UserProfileCtrl.$inject = [
    'gettextCatalog',
    'modalService',
    'modalInput',
    'notificationsService',
    'usersService',
    'User'
  ];

  /**
   * Displays a list of users in a table.
   */
  function UserProfileCtrl(gettextCatalog,
                           modalService,
                           modalInput,
                           notificationsService,
                           usersService,
                           User) {
    var vm = this;

    vm.updateName      = updateName;
    vm.updateEmail     = updateEmail;
    vm.updatePassword  = updatePassword;
    vm.updateAvatarUrl = updateAvatarUrl;
    vm.removeAvatarUrl = removeAvatarUrl;

    init();

    //--

    function init() {
      usersService.requestCurrentUser().then(function (user) {
        vm.user = User.create(user);
        vm.allowRemoveAvatarUrl = (vm.user.avatarUrl !== null);
      });
    }

    function updateError(err) {
      notificationsService.updateError(
        gettextCatalog.getString('Your change could not be saved: ') + err.data.message,
        gettextCatalog.getString('Cannot apply your change'));
    }

    function postUpdate(message, title, timeout) {
      timeout = timeout || 1500;
      return function (user) {
        vm.user = user;
        vm.allowRemoveAvatarUrl = (vm.user.avatarUrl !== null);
        notificationsService.success(message, title, timeout);
      };
    }

    function updateName() {
      var name = vm.user.name;

      modalInput.text(gettextCatalog.getString('Update user name'),
                      gettextCatalog.getString('Name'),
                      name,
                      { required: true, minLength: 2 })
        .result.then(function (name) {
          vm.user.updateName(name)
            .then(postUpdate(gettextCatalog.getString('User name updated successfully.'),
                             gettextCatalog.getString('Update successful')))
            .catch(updateError);
        });
    }

    function updateEmail() {
      modalInput.email(gettextCatalog.getString('Update user email'),
                       gettextCatalog.getString('Email'),
                       vm.user.email,
                       { required: true })
        .result.then(function (email) {
        vm.user.updateEmail(email)
          .then(postUpdate(gettextCatalog.getString('Email updated successfully.'),
                           gettextCatalog.getString('Update successful')))
          .catch(updateError);
      });
    }

    function updateAvatarUrl() {
      modalInput.url(gettextCatalog.getString('Update avatar URL'),
                     gettextCatalog.getString('Avatar URL'),
                     vm.user.avatarUrl)
        .result.then(function (avatarUrl) {
          vm.user.updateAvatarUrl(avatarUrl)
            .then(postUpdate(gettextCatalog.getString('Avatar URL updated successfully.'),
                             gettextCatalog.getString('Update successful')))
            .catch(updateError);
        });
    }

    function removeAvatarUrl() {
      modalService.modalOkCancel(gettextCatalog.getString('Remove Avatar URL'),
                                 gettextCatalog.getString('Are you sure you want to remove your Avatar URL?'))
        .then(function() {
          vm.user.updateAvatarUrl(null)
            .then(postUpdate(gettextCatalog.getString('Avatar URL remove successfully.'),
                             gettextCatalog.getString('Remove successful')))
            .catch(updateError);
        });
    }

    function updatePassword() {
      modalInput.password(gettextCatalog.getString('Change password')).result
        .then(function (result) {
          vm.user.updatePassword(result.currentPassword, result.newPassword)
            .then(postUpdate(gettextCatalog.getString('Your password was updated successfully.'),
                             gettextCatalog.getString('Update successful')))
            .catch(function (err) {
              if (err.data.message.indexOf('invalid password') > -1) {
                notificationsService.error(
                  gettextCatalog.getString('Your current password was incorrect.'),
                  gettextCatalog.getString('Cannot update your password'));
              } else {
                updateError(err);
              }
            });
        });
    }
  }

  return userProfileDirective;
});
