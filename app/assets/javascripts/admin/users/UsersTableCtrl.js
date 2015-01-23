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
    'usersService',
    'UserModalService',
    'userCount'
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
                          usersService,
                          UserModalService,
                          userCount) {
    var vm = this;

    vm.users = [];
    vm.haveUsers = (userCount > 0);
    vm.paginatedUsers = {};

    vm.nameFilter       = '';
    vm.emailFilter       = '';
    vm.possibleStatuses = [
      { id: 'all',        title: 'All' },
      { id: 'active',     title: 'Active' },
      { id: 'registered', title: 'Registered' },
      { id: 'locked',     title: 'Locked' }
    ];
    vm.status              = vm.possibleStatuses[0];
    vm.userInformation     = userInformation;
    vm.activate            = activate;
    vm.lock                = lock;
    vm.unlock              = unlock;
    vm.nameFilterUpdated   = nameFilterUpdated;
    vm.emailFilterUpdated  = emailFilterUpdated;
    vm.statusFilterUpdated = statusFilterUpdated;

    var tableParameters = {
      page: 1,
      count: 10,
      sorting: {
        name: 'asc'
      }
    };

    var tableSettings = {
      total: 0,
      getData: getData
    };

    vm.tableParams = tableService.getTableParams(vm.users, tableParameters, tableSettings);
    updateMessage();

    // --

    function updateMessage() {
      if ((vm.nameFilter === '') && (vm.status.id === 'all')) {
        vm.message = 'The following users have been configured.';
      } else {
        vm.message = 'The following users match the criteria:';
      }
    }

    function getData($defer, params) {
      var sortObj = params.sorting();
      var sortKeys = _.keys(sortObj);

      usersService.getUsers(vm.nameFilter,
                            vm.emailFilter,
                            vm.status.id,
                            sortKeys[0],
                            params.page(),
                            params.count(),
                            sortObj[sortKeys[0]])
        .then(function (paginatedUsers) {
          vm.paginatedUsers = paginatedUsers;
          vm.users = [];
          _.each(paginatedUsers.items, function(user) {
            vm.users.push(_.extend(
              user, {timeAddedLocal: (new Date(user.timeAdded)).toLocaleString()}));
          });
          vm.paginatedUsers = paginatedUsers;
          params.total(paginatedUsers.total);
          $defer.resolve(vm.users);
          updateMessage();
        });
    }

    function tableReloadCommon() {
      vm.tableParams.page(1);
      vm.tableParams.reload();
    }

    /**
     * Called when user enters text into the 'name filter'.
     */
    function nameFilterUpdated() {
      tableReloadCommon();
    }

    /**
     * Called when user enters text into the 'email filter'.
     */
    function emailFilterUpdated() {
      tableReloadCommon();
    }

    /**
     * Called when user selects a status from the 'status filter' select.
     */
    function statusFilterUpdated() {
      tableReloadCommon();
    }

    function changeStatus(user, statusChangeFn, status) {
      var modalOptions = {
        closeButtonText: 'Cancel',
        actionButtonText: 'OK'
      };

      modalOptions.headerHtml = 'Change user status';
      modalOptions.bodyHtml = 'Please confirm that you want to ' + status + ' user "' +
        user.name + '"?';

      modalService.showModal({}, modalOptions)
        .then(function () {
          statusChangeFn(user).then(function() {
            vm.tableParams.reload();
          });
        }
      );
    }

    function userInformation(user) {
      UserModalService.show(user);
    }

    function activate(user) {
      changeStatus(user, usersService.activate, 'activate');
    }

    function lock(user) {
      changeStatus(user, usersService.lock, 'lock');
    }

    function unlock(user) {
      changeStatus(user, usersService.unlock, 'unlock');
    }
  }

});
