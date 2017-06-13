/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/users/components/userProfile/userProfile.html',
    controller: UserProfileController,
    controllerAs: 'vm',
    bindings: {
    }
  };

  UserProfileController.$inject = [
    '$state',
    'gettextCatalog',
    'modalService',
    'modalInput',
    'notificationsService',
    'usersService',
    'User'
  ];

  /*
   * Controller for this component.
   */
  function UserProfileController($state,
                                 gettextCatalog,
                                 modalService,
                                 modalInput,
                                 notificationsService,
                                 usersService,
                                 User) {
    var vm = this;

    vm.$onInit           = onInit;
    vm.studyMemberships  = '';
    vm.centreMemberships = '';
    vm.updateName        = updateName;
    vm.updateEmail       = updateEmail;
    vm.updatePassword    = updatePassword;
    vm.updateAvatarUrl   = updateAvatarUrl;
    vm.removeAvatarUrl   = removeAvatarUrl;

    //--

    function onInit() {
      usersService.requestCurrentUser()
        .then(function (user) {
          vm.user = User.create(user);
          vm.allowRemoveAvatarUrl = (vm.user.avatarUrl !== null);
          if (vm.user.membership.studyInfo.all) {
            vm.studyMemberships = gettextCatalog.getString('All Studies');
          } else if (vm.user.membership.studyInfo.names.length > 0){
             vm.studyMemberships = vm.user.membership.studyInfo.names.join(', ');
          } else {
            vm.studyMemberships = gettextCatalog.getString('None');
          }

          if (vm.user.membership.centreInfo.all) {
            vm.centreMemberships = gettextCatalog.getString('All Centres');
          } else if (vm.user.membership.centreInfo.names.length > 0){
             vm.centreMemberships = vm.user.membership.centreInfo.names.join(', ');
          } else {
             vm.centreMemberships = gettextCatalog.getString('None');
          }
        })
        .catch(function (error) {
          if (error.status && (error.status === 401)) {
            $state.go('home.users.login', {}, { reload: true });
          }
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
            .then(function (user) {
              postUpdate(gettextCatalog.getString('User name updated successfully.'),
                         gettextCatalog.getString('Update successful'))(user);
              usersService.retrieveCurrentUser();
            })
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

  return component;
});
