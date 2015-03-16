define(['./module'], function(module) {
  'use strict';

  /**
   * Displays a list of users in a table.
   */
  module.controller('UserProfileCtrl', UserProfileCtrl);

  UserProfileCtrl.$inject = [
    '$modal',
    'notificationsService',
    'modalService',
    'User',
    'user'
  ];

  function UserProfileCtrl($modal,
                           notificationsService,
                           modalService,
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

    function postUpdate(userId, message, title, timeout) {
      return function (user) {
        vm.user = user;
        console.log(vm.user.timeAdded, vm.user.timeModified);
        notificationsService.success(message, title, timeout);
      };
    }

    function updateName() {
      modalStringInput(modalInputTypes.text, 'Update user name', 'Name', vm.user.name).result
        .then(function (name) {
          vm.user.updateName(name)
            .then(postUpdate(vm.user.id,
                             'User name updated successfully.',
                             'Update successful',
                             1500))
            .catch(updateError);
        });
    }

    function updateEmail() {
      modalStringInput(modalInputTypes.email, 'Update user email', 'Email', vm.user.email).result
        .then(function (email) {
          vm.user.updateEmail(email)
            .then(postUpdate(vm.user.id,
                             'Email updated successfully.',
                             'Update successful',
                             1500))
            .catch(updateError);
        });
    }

    function updateAvatarUrl() {
      modalStringInput(modalInputTypes.url, 'Update avatar URL', 'Avatar URL', vm.user.avatarUrl).result
        .then(function (avatarUrl) {
          vm.user.updateAvatarUrl(avatarUrl)
            .then(postUpdate(vm.user.id,
                  'Avatar URL updated successfully.',
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
            .then(postUpdate(vm.user.id,
                'Avatar URL remove successfully.',
                'Remove successful',
                1500))
            .catch(updateError);
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
        vm.user.updatePassword(result.currentPassword, result.newPassword)
          .then(postUpdate(vm.user.id,
                           'Your password was updated successfully.',
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

});
