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
    '$log',
    'AppConfig',
    'User',
    'UserCounts',
    'UserState',
    'UserViewer',
    'modalService',
    'filterExpression',
    'gettextCatalog'
  ];

  /**
   *
   */
  function UsersTableController($log,
                                AppConfig,
                                User,
                                UserCounts,
                                UserState,
                                UserViewer,
                                modalService,
                                filterExpression,
                                gettextCatalog) {
    var vm = this;

    vm.users            = [];
    vm.possibleStates   = getPossibleStates();
    vm.state            = 'all';
    vm.getTableData     = getTableData;
    vm.tableDataLoading = true;
    vm.limit            = 10;

    vm.userInformation  = userInformation;
    vm.activate         = activate;
    vm.lock             = lock;
    vm.unlock           = unlock;

    //--

    function getPossibleStates() {
      return [ { id: 'all', title: 'All' } ].concat(
        _.map(_.values(UserState), function(state) {
          return { id: state, title: state.toUpperCase() };
        }));
    }

    function getTableData(tableState) {
      var pagination            = tableState.pagination,
          searchPredicateObject = tableState.search.predicateObject || {},
          sortPredicate         = tableState.sort.predicate || 'email',
          sortOrder             = tableState.sort.reverse || false,
          nameFilter            = searchPredicateObject.name ? '*' + searchPredicateObject.name + '*': '',
          emailFilter           = searchPredicateObject.email ? '*' + searchPredicateObject.email + '*' : '',
          options = {
            filter: filterExpression.create(
              [
                { key: 'name', value: nameFilter },
                { key: 'email', value: emailFilter },
                { key: 'state', value: (vm.state !== 'all') ? vm.state : ''
                }
              ]),
            sort:        (sortOrder ? '-' : '') + sortPredicate,
            page:        1 + (pagination.start / vm.limit),
            limit:       vm.limit
          };

      vm.tableDataLoading = true;

      User.list(options).then(function (paginatedUsers) {
        vm.users = paginatedUsers.items;
        tableState.pagination.numberOfPages = paginatedUsers.maxPages;

        vm.tableDataLoading = false;
      });
    }

    function changeState(user, method, state) {
      var index = vm.users.indexOf(user);

      if (index < 0) {
        $log.error('user not found');
        throw new Error('user not found');
      }

      modalService.modalOkCancel(
        gettextCatalog.getString('Change user state'),
        gettextCatalog.getString('Please confirm that you want to {{stateAction}} user {{userName}}',
                                 {
                                   stateAction: state,
                                   userName: user.name
                                 }))
        .then(function () { return user[method](); })
        .then(function (updatedUser) {
          vm.users[index] = updatedUser;
        });
    }

    function activate(user) {
      changeState(user, 'activate', 'activate');
    }

    function lock(user) {
      changeState(user, 'lock', 'lock');
    }

    function unlock(user) {
      changeState(user, 'unlock', 'unlock');
    }

    function userInformation(user) {
      return new UserViewer(user);
    }

  }

  return component;
});
