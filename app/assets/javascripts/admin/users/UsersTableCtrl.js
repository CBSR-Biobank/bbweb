define(['angular', 'underscore', 'moment'], function(angular, _, moment) {
  'use strict';

  UsersTableCtrl.$inject = [
    'modalService',
    'tableService',
    'User',
    'UserStatus',
    'UserViewer',
    'userCounts'
  ];

  /**
   * Displays a list of users in a table.
   */
  function UsersTableCtrl(modalService,
                          tableService,
                          User,
                          UserStatus,
                          UserViewer,
                          userCounts) {
    var vm = this;

    vm.users               = [];
    vm.haveUsers           = (userCounts.total > 0);
    vm.pagedResults      = {};
    vm.nameFilter          = '';
    vm.emailFilter         = '';
    vm.possibleStatuses    = getPossibleStatuses();
    vm.status              = vm.possibleStatuses[0];
    vm.tableParams         = getTableParams();

    vm.userInformation     = userInformation;
    vm.activate            = activate;
    vm.lock                = lock;
    vm.unlock              = unlock;
    vm.nameFilterUpdated   = nameFilterUpdated;
    vm.emailFilterUpdated  = emailFilterUpdated;
    vm.statusFilterUpdated = statusFilterUpdated;
    vm.getTimeAddedlocal   = getTimeAddedlocal;

    updateMessage();

    // --

    function getPossibleStatuses() {
      var result = _.map(UserStatus.values(), function(status) {
        return { id: status.toLowerCase(), title: status };
      });
      result.unshift({ id: 'all', title: 'All' });
      return result;
    }

    function getTableParams() {
      var tableParameters = { page: 1,
                              count: 10,
                              sorting: {
                                name: 'asc'
                              }
                            },
          tableSettings = { total: 0,
                            getData: getTableData
                          };

      return tableService.getTableParams(vm.users, tableParameters, tableSettings);

      function getTableData($defer, params) {
        var sortObj = params.sorting();
        var sortKeys = _.keys(sortObj);
        var options = {
          nameFilter:  vm.nameFilter,
          emailFilter: vm.emailFilter,
          status:      vm.status.id,
          sort:        sortKeys[0],
          page:        params.page(),
          pageSize:    params.count(),
          order:       sortObj[sortKeys[0]]
        };

        User.list(options).then(function (paginatedUsers) {
          vm.pagedResults = paginatedUsers;
          vm.users = paginatedUsers.items;
          vm.pagedResults = paginatedUsers;
          params.total(paginatedUsers.total);
          $defer.resolve(vm.users);
          updateMessage();
        });
      }
    }

    function updateMessage() {
      if ((vm.nameFilter === '') && (vm.status.id === 'all')) {
        vm.message = 'The following users have been configured.';
      } else {
        vm.message = 'The following users match the criteria:';
      }
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

    function changeStatus(user, method, status) {
      var modalOptions = {
        closeButtonText: 'Cancel',
        actionButtonText: 'OK'
      };

      modalOptions.headerHtml = 'Change user status';
      modalOptions.bodyHtml = 'Please confirm that you want to ' + status + ' user "' +
        user.name + '"?';

      modalService.showModal({}, modalOptions).then(function () {
        user[method]().then(function() {
          vm.tableParams.reload();
        });
      });
    }

    function userInformation(user) {
      return new UserViewer(user);
    }

    function activate(user) {
      changeStatus(user, 'activate', 'activate');
    }

    function lock(user) {
      changeStatus(user, 'lock', 'lock');
    }

    function unlock(user) {
      changeStatus(user, 'unlock', 'unlock');
    }

    function getTimeAddedlocal(user) {
      return moment(user.timeAdded).format('YYYY-MM-DD hh: ss A');
    }
  }

  return UsersTableCtrl;
});
