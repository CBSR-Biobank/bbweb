define(['../module', 'angular', 'underscore'], function(module, angular, _) {
  'use strict';

  module.controller('UsersTableCtrl', UsersTableCtrl);

  UsersTableCtrl.$inject = [
    '$rootScope',
    '$scope',
    '$state',
    '$filter',
    'stateHelper',
    'modalService',
    'tableService',
    'userService',
    'UserModalService'
  ];

  /**
   * Displays a list of users in a table.
   */
  function UsersTableCtrl($rootScope,
                          $scope,
                          $state,
                          $filter,
                          stateHelper,
                          modalService,
                          tableService,
                          userService,
                          UserModalService) {
    var vm = this;

    vm.users = [];

    vm.userInformation = userInformation;
    vm.activate        = activate;
    vm.lock            = lock;
    vm.unlock          = unlock;

    vm.tableParams = tableService.getTableParamsWithCallback(getTableData);
    vm.tableParams.settings().$scope = $scope;  // kludge: see https://github.com/esvit/ng-table/issues/297#issuecomment-55756473
    updateData();

    // --

    function getTableData() {
      return vm.users;
    }

    function updateData() {
      userService.getAllUsers().then(function(data) {
        vm.users = [];
        _.each(data, function(user) {
          vm.users.push(angular.extend(
            user, {timeAddedLocal: (new Date(user.timeAdded)).toLocaleString()}));
        });
        vm.tableParams.reload();
      });
    }

    function changeStatus(user, statusChangeFn, status) {
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
    }

    function userInformation(user) {
      UserModalService.show(user);
    }

    function activate(user) {
      changeStatus(user, userService.activate, 'activate');
    }

    function lock(user) {
      changeStatus(user, userService.lock, 'lock');
    }

    function unlock(user) {
      changeStatus(user, userService.unlock, 'unlock');
    }
  }

});
