/**
 * User administration controllers.
 */
define(['angular', 'underscore', 'common'], function(angular, _) {
  'use strict';

  var mod = angular.module('admin.users.controllers', ['biobank.common', 'users.services']);

  /**
   * Displays a list of users in a table.
   */
  mod.controller('UsersTableCtrl', [
    '$rootScope',
    '$scope',
    '$state',
    '$filter',
    'stateHelper',
    'modalService',
    'panelTableService',
    'userService',
    'UserModalService',
    function($rootScope,
             $scope,
             $state,
             $filter,
             stateHelper,
             modalService,
             panelTableService,
             userService,
             UserModalService) {

      var updateData = function() {
        userService.getAllUsers().then(function(data) {
          $scope.users = [];
          _.each(data, function(user) {
            $scope.users.push(angular.extend(
              user, {timeAddedLocal: (new Date(user.timeAdded)).toLocaleString()}));
          });
          $scope.tableParams.reload();
        });
      };

      var getTableData = function() {
        return $scope.users;
      };

      var changeStatus = function(user, statusChangeFn, status) {
        var modalOptions = {
          closeButtonText: 'Cancel',
          actionButtonText: 'OK'
        };

        modalOptions.headerText = 'Change user status';
        modalOptions.bodyText = 'Please confirm that you want to ' + status + ' user "' +
          user.name + '"?';

        modalService.showModal({}, modalOptions).then(
          function() {
            statusChangeFn(user).then(function() {
              updateData();
            });
          }
        );
      };

      $rootScope.pageTitle = 'Biobank users';
      $scope.users = [];

      $scope.tableParams = panelTableService.getTableParamsWithCallback(getTableData);
      $scope.tableParams.settings().$scope = $scope;  // kludge: see https://github.com/esvit/ng-table/issues/297#issuecomment-55756473
      updateData();

      $scope.userInformation = function(user) {
        UserModalService.show(user);
      };

      $scope.activate = function(user) {
        changeStatus(user, userService.activate, 'activate');
      };

      $scope.lock = function(user) {
        changeStatus(user, userService.lock, 'lock');
      };

      $scope.unlock = function(user) {
        changeStatus(user, userService.unlock, 'unlock');
      };

    }
  ]);

  /**
   * Displays a list of users in a table.
   */
  mod.controller('UserUpdateCtrl', [
    '$rootScope', '$scope', '$state', '$filter', 'userService', 'modalService', 'stateHelper', 'user',
    function($rootScope, $scope, $state, $filter, userService, modalService, stateHelper, user) {

      var onError = function (error) {
        var modalOptions = {
          closeButtonText: 'Cancel',
          actionButtonText: 'OK'
        };

        if (error.message.indexOf('expected version doesn\'t match current version') > -1) {
          /* concurrent change error */
          modalOptions.headerText = 'Modified by another user';
          modalOptions.bodyText = 'Another user already made changes to this user. Press OK to make ' +
            ' your changes again, or Cancel to dismiss your changes.';
        } else {
          /* some other error */
          modalOptions.headerText = 'Cannot update user';
          modalOptions.bodyText = error.message;
        }

        modalService.showModal({}, modalOptions).then(
          function () {
            stateHelper.reloadAndReinit();
          },
          function () {
            $state.go('admin.users');
          });
      };

      $scope.form = {
        user: user,
        password: '',
        confirmPassword: '',
        submit: function(user, password) {
          userService.update(user, password).then(
            function() {
              $state.go('admin.users');
            },
            onError
          );
        },
        cancel: function() {
          $state.go('home');
        }
      };
    }
  ]);

  return mod;
});
