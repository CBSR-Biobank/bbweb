/**
 * User administration controllers.
 */
define(['angular', 'underscore', 'common'], function(angular, _, common) {
  'use strict';

  var mod = angular.module('admin.users.controllers', ['users.services']);

  /**
   * Displays a list of users in a table.
   *
   * "user" is not a service, but stems from userResolve (Check ../user/services.js) object.
   */
  mod.controller('UsersTableCtrl', [
    '$rootScope', '$scope', '$state', '$filter', 'ngTableParams', 'userService',
    function($rootScope, $scope, $state, $filter, ngTableParams, userService) {
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

        $scope.userInformation = function(user) {
          $state.go("admin.users.user", { userId: user.id });
        };
      });
    }]);

  return mod;
});
