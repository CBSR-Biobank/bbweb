/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/admin/components/users/usersTable/usersTable.html',
    controller: UsersTableController,
    controllerAs: 'vm',
    bindings: {
      userCounts: '<'
    }
  };

  UsersTableController.$inject = [
    'AppConfig',
    'User',
    'UserCounts',
    'UserStatus',
    'UserViewer',
    'userStatusLabel',
    'modalService'
  ];

  /**
   *
   */
  function UsersTableController(AppConfig,
                                User,
                                UserCounts,
                                UserStatus,
                                UserViewer,
                                userStatusLabel,
                                modalService) {
    var vm = this;

    vm.users               = [];
    vm.possibleStatuses    = getPossibleStatuses();
    vm.status              = 'all';
    vm.getTableData        = getTableData;
    vm.tableDataLoading    = true;
    vm.limit            = 10;

    vm.userInformation     = userInformation;
    vm.activate            = activate;
    vm.lock                = lock;
    vm.unlock              = unlock;

    //--

    function getPossibleStatuses() {
      return [ { id: 'all', title: 'All' } ].concat(
        _.map(_.values(UserStatus), function(status) {
          return { id: status, title: userStatusLabel.statusToLabel(status) };
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
            page:        1 + (pagination.start / vm.limit),
            limit:    vm.limit,
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
      var index = vm.users.indexOf(user),
          modalOptions = {
            closeButtonText: 'Cancel',
            actionButtonText: 'OK',
            headerHtml: 'Change user status',
            bodyHtml: 'Please confirm that you want to ' + status + ' user "' + user.name + '"?'
          };

      modalService.showModal({}, modalOptions)
        .then(function () { return user[method](); })
        .then(function() { return User.get(user.id); })
        .then(function (updatedUser) {
          if (index < 0) {
            console.error('cannot not update user');
            return;
          }
          vm.users[index] = updatedUser;
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

  }

  return component;
});
