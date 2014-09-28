/**
 * User administration controllers.
 */
define(['angular', 'underscore', 'common'], function(angular) {
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
    'ngTableParams',
    'userService',
    'UserModalService',
    function($rootScope,
             $scope,
             $state,
             $filter,
             stateHelper,
             modalService,
             ngTableParams,
             userService,
             UserModalService) {

      var updateData = function() {
        userService.getAllUsers().then(function(data) {
          $scope.users = data;
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

      /* jshint -W055 */
      $scope.tableParams = new ngTableParams({
        page: 1,            // show first page
        count: 10,          // count per page
        sorting: {
          name: 'asc'       // initial sorting
        }
      }, {
        counts: [], // hide page counts control
        total: function () { return getTableData().length; },
        getData: function($defer, params) {
          var filteredData = getTableData();
          var orderedData = params.sorting() ?
              $filter('orderBy')(filteredData, params.orderBy()) : filteredData;
          $defer.resolve(orderedData.slice(
            (params.page() - 1) * params.count(),
            params.page() * params.count()));
        }
      });
      /* jshint +W055 */

      $scope.tableParams.settings().$scope = $scope;
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
