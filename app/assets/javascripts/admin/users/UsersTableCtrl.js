/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'underscore', 'moment'], function(angular, _, moment) {
  'use strict';

  UsersTableCtrl.$inject = [
    'modalService',
    'User',
    'UserStatus',
    'UserViewer',
    'userCounts',
    'bbwebConfig'
  ];

  /**
   * Displays a list of users in a table.
   */
  function UsersTableCtrl(modalService,
                          User,
                          UserStatus,
                          UserViewer,
                          userCounts,
                          bbwebConfig) {
    var vm = this;

    vm.users               = [];
    vm.haveUsers           = (userCounts.total > 0);
    vm.possibleStatuses    = getPossibleStatuses();
    vm.getTableData        = getTableData;
    vm.tableDataLoading    = true;
    vm.pageSize            = 10;

    vm.userInformation     = userInformation;
    vm.activate            = activate;
    vm.lock                = lock;
    vm.unlock              = unlock;
    vm.getTimeAddedLocal   = getTimeAddedLocal;

    // used by the status filter box shown in the table
    vm.status              = vm.possibleStatuses[0].id;

    // --

    function getPossibleStatuses() {
      return [{ id: 'all', title: 'All' }].concat(
        _.map(UserStatus.values(), function(status) {
          return { id: status, title: UserStatus.label(status) };
        }));
    }

    function getTableData(tableState) {
      var pagination            = tableState.pagination,
          searchPredicateObject = tableState.search.predicateObject || {},
          sortPredicate         = tableState.sort.predicate || 'email',
          sortOrder             = tableState.sort.reverse || false,
          options = {
            nameFilter:  searchPredicateObject.name || '',
            emailFilter: searchPredicateObject.email || '',
            status:      vm.status,
            sort:        sortPredicate,
            page:        1 + (pagination.start / vm.pageSize),
            pageSize:    vm.pageSize,
            order:       sortOrder ? 'desc' : 'asc'
          };

      vm.tableDataLoading = true;

      User.list(options).then(function (paginatedUsers) {
        vm.users = paginatedUsers.items;
        tableState.pagination.numberOfPages = paginatedUsers.maxPages;

        vm.tableDataLoading = false;
      });
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
          var index = vm.users.indexOf(user);
          if (index !== -1) {
            User.get(user.id).then(function (reloadedUser) {
              vm.users[index] = reloadedUser;
            });
          }
        });
      });
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

    function userInformation(user) {
      return new UserViewer(user);
    }

    function getTimeAddedLocal(user) {
      return moment(user.timeAdded).format(bbwebConfig.dateTimeFormat);
    }
  }

  return UsersTableCtrl;
});
