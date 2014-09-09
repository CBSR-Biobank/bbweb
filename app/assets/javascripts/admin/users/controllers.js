/**
 * User administration controllers.
 */
define(['angular', 'underscore', 'common'], function(angular, _, common) {
  'use strict';

  var mod = angular.module('admin.users.controllers', ['users.services']);

  /**
   * Displays a list of users in a table.
   */
  mod.controller('UsersTableCtrl', [
    '$q',
    '$rootScope',
    '$scope',
    '$state',
    '$filter',
    'stateHelper',
    'modalService',
    'ngTableParams',
    'userService',
    'UserModalService',
    function($q,
             $rootScope,
             $scope,
             $state,
             $filter,
             stateHelper,
             modalService,
             ngTableParams,
             userService,
             UserModalService) {
      $rootScope.pageTitle = 'Biobank users';
      $scope.users = [];
      userService.getAllUsers().then(function(data) {
        $scope.users = data;

        /* jshint ignore:start */
        $scope.tableParams = new ngTableParams({
          page: 1,            // show first page
          count: 10,          // count per page
          sorting: {
            name: 'asc'       // initial sorting
          }
        }, {
          counts: [], // hide page counts control
          total: $scope.users.length,
          getData: function($defer, params) {
            var orderedData = params.sorting()
              ? $filter('orderBy')($scope.users, params.orderBy())
              : $scope.users;
            $defer.resolve(orderedData.slice(
              (params.page() - 1) * params.count(),
              params.page() * params.count()));
          }
        });
        /* jshint ignore:end */
      });

      var changeStatusModal = function(user, status) {
        var modalOptions = {
          closeButtonText: 'Cancel',
          actionButtonText: 'OK'
        };

        modalOptions.headerText = 'Change user status';
        modalOptions.bodyText = 'Please confirm that you want to ' + status + ' user "' +
          user.name + '"?';

        return modalService.showModal({}, modalOptions);
      };

      //$state.go("admin.users.user", { userId: user.id });

      $scope.userInformation = function(user) {
        UserModalService.show(user);
      };

      $scope.activate = function(user) {
        changeStatusModal(user, 'activate').then(
          function(result) {
            userService.activate(user);
            stateHelper.reloadAndReinit();
          },
          stateHelper.reloadAndReinit()
        );
      };

      $scope.lock = function(user) {
        changeStatusModal(user, 'lock').then(
          function(result) {
            userService.lock(user);
            stateHelper.reloadAndReinit();
          },
          stateHelper.reloadAndReinit()
        );
      };

      $scope.unlock = function(user) {
        changeStatusModal(user, 'unlock').then(
          function(result) {
            userService.unlock(user);
            stateHelper.reloadAndReinit();
          },
          stateHelper.reloadAndReinit()
        );
      };

    }]);

  /**
   * Displays a list of users in a table.
   */
  mod.controller('UserUpdateCtrl', [
    '$rootScope', '$scope', '$state', '$filter', 'userService', 'modalService', 'user',
    function($rootScope, $scope, $state, $filter, userService, modalService, user) {

      var onError = function (error) {
        var modalOptions = {
          closeButtonText: 'Cancel',
          actionButtonText: 'OK'
        };

        if (error.message.indexOf("expected version doesn't match current version") > -1) {
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
          function (result) {
            stateHelper.reloadAndReinit();
          },
          function () {
            $state.go("admin.users");
          });
      };

      $scope.form = {
        user: user,
        password: '',
        confirmPassword: '',
        submit: function(user, password) {
          userService.update(user, password).then(
            function(event) {
              $state.go("admin.users");
            },
            onError
          );
        },
        cancel: function() {
            $state.go("home");
        }
      };
    }]);

  return mod;
});
