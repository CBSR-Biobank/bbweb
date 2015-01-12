define(['./module'], function(module) {
  'use strict';

  /**
   * Displays a list of users in a table.
   */
  module.controller('UserProfileCtrl', UserProfileCtrl);

  UserProfileCtrl.$inject = [
    '$filter',
    '$modal',
    'domainEntityUpdateError',
    'notificationsService',
    'modalService',
    'usersService',
    'stateHelper',
    'user'
  ];

  function UserProfileCtrl($filter,
                           $modal,
                           domainEntityUpdateError,
                           notificationsService,
                           modalService,
                           usersService,
                           stateHelper,
                           user) {
    var vm = this;

    vm.user            = user;
    vm.updateName      = updateName;
    vm.updateEmail     = updateEmail;
    vm.updatePassword  = updatePassword;
    vm.updateAvatarUrl = updateAvatarUrl;
    vm.removeAvatarUrl = removeAvatarUrl;

    //--

    var modalInputTypes = {
      text: 'text',
      email: 'email',
      url: 'url'
    };

    function updateError(err) {
      notificationsService.error(
        'Your change could not be saved: ' + err.data.message,
        'Cannot apply your change');
    }

    function modalStringInput(type, title, label, defaultValue) {
      controller.$inject = ['$scope', '$modalInstance', 'defaultValue'];

      function controller ($scope, $modalInstance, defaultValue) {
        $scope.modal = {
          value: defaultValue,
          type: type,
          title: title,
          label: label
        };

        $scope.modal.ok = function () {
          console.log('$scope.modal.ok', $scope.modal.value);
          $modalInstance.close($scope.modal.value);
        };
        $scope.modal.close = function () {
          $modalInstance.dismiss('cancel');
        };
      }

      return $modal.open({
        templateUrl: '/assets/javascripts/users/userUpdateModal.html',
        controller: controller,
        resolve: {
          defaultValue: function () {
            return defaultValue;
          }
        },
        backdrop: true,
        keyboard: true,
        modalFade: true
      });
    }

    function updateName() {
      modalStringInput(modalInputTypes.text, 'Update user name', 'Name', user.name).result
        .then(function (name) {
          console.log(name);
          usersService.updateName(user, name)
            .then(function (event) {
              vm.user.name = event.name;
              vm.user.version = event.version;

              notificationsService.success(
                'User name updated successfully.',
                'Update successful',
                1500);
            })
            .catch(updateError);
        });
    }

    function updateEmail() {
      modalStringInput(modalInputTypes.email, 'Update user email', 'Email', user.email).result
        .then(function (email) {
          console.log(email);
          usersService.updateEmail(user, email)
            .then(function (event) {
              vm.user.email = event.email;
              vm.user.version = event.version;

              notificationsService.success(
                'Email updated successfully.',
                'Update successful',
                1500);
            })
            .catch(updateError);
        });
    }

    function updateAvatarUrl() {
      modalStringInput(modalInputTypes.url, 'Update avatar URL', 'Avatar URL', user.avatarUrl).result
        .then(function (avatarUrl) {
          usersService.updateAvatarUrl(user, avatarUrl)
            .then(function (event) {
              vm.user.avatarUrl = event.avatarUrl;
              vm.user.version = event.version;

              notificationsService.success(
                'Avatar URL updated successfully.',
                'Update successful',
                1500);
            })
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
          usersService.updateAvatarUrl(user, null)
            .then(function (event) {
              vm.user.avatarUrl = event.avatarUrl;
              vm.user.version = event.version;

              notificationsService.success(
                'Avatar URL remove successfully.',
                'Remove successful',
                1500);
            });
        });
    }

    function updatePassword() {
      var modalInstance = $modal.open({
        templateUrl: '/assets/javascripts/users/userUpdatePasswordModal.html',
        controller: ['$scope', '$modalInstance', function ($scope, $modalInstance) {
          $scope.modal = {
            currentPassword: '',
            newPassword: '',
            confirmPassword: ''
          };
          $scope.modal.ok = function () {
            $modalInstance.close({
              currentPassword: $scope.modal.currentPassword,
              newPassword: $scope.modal.newPassword
            });
          };
          $scope.modal.close = function () {
            $modalInstance.dismiss('cancel');
          };
        }]
      });

      modalInstance.result.then(function (result) {
        usersService.updatePassword(user, result.currentPassword, result.newPassword)
          .then(function (event) {
            vm.user.version = event.version;

            notificationsService.success(
              'Password was updated successfully.',
              'Update successful',
              1500);
          })
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

});
