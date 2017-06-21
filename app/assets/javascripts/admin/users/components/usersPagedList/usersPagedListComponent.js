/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  /**
   * Displays studies in a panel list.
   *
   * @return {object} An AngularJS component.
   */
  var component = {
    templateUrl: '/assets/javascripts/admin/users/components/usersPagedList/usersPagedList.html',
    controller: StudiesPagedListController,
    controllerAs: 'vm',
    bindings: {
    }
  };

  StudiesPagedListController.$inject = [
    '$controller',
    '$scope',
    '$state',
    'User',
    'UserState',
    'userStateLabel',
    'UserCounts',
    'gettextCatalog'
  ];

  /*
   * Controller for this component.
   */
  function StudiesPagedListController($controller,
                                      $scope,
                                      $state,
                                      User,
                                      UserState,
                                      userStateLabel,
                                      UserCounts,
                                      gettextCatalog) {
    var vm = this;

    vm.$onInit              = onInit;
    vm.emailFilter          = '';
    vm.counts               = {};
    vm.limit                = 5;
    vm.getItems             = getItems;
    vm.getItemIcon          = getItemIcon;
    vm.emailFilterUpdated   = emailFilterUpdated;

    // initialize this controller's base class
    $controller('PagedListController', {
      vm:             vm,
      gettextCatalog: gettextCatalog
    });

    vm.superGetFilters      = vm.getFilters;
    vm.getFilters           = getFilters;
    vm.superFiltersCleared  = vm.filtersCleared;
    vm.filtersCleared       = filtersCleared;

    vm.stateData = _.map(_.values(UserState), function (state) {
      return { id: state, label: userStateLabel.stateToLabel(state) };
    });

    vm.stateLabels = {};
    _.forEach(_.values(UserState), function (state) {
      vm.stateLabels[state] = userStateLabel.stateToLabel(state);
    });

    // override this setting
    vm.sortFields = [
      gettextCatalog.getString('Name'),
      gettextCatalog.getString('Email'),
      gettextCatalog.getString('State')
    ];

    //--

    function onInit() {
      UserCounts.get()
        .then(function (counts) {
          vm.userCounts = counts;
          vm.haveUsers  = (vm.userCounts.total > 0);
        })
        .catch(function (error) {
          if (error.status && (error.status === 401)) {
            $state.go('home.users.login', {}, { reload: true });
          }
        });
    }

    function getItems(options) {
      // KLUDGE: for now, fix after Entity Pagers have been implemented
      return UserCounts.get().then(function (counts) {
        vm.counts = counts;
        return User.list(options);
      });
    }

    function getItemIcon(user) {
      if (user.isRegistered()) {
        return 'glyphicon-cog';
      } else if (user.isActive()) {
        return 'glyphicon-user';
      } else if (user.isLocked()) {
        return 'glyphicon-lock';
      }
      throw new Error('invalid user state: ' + user.state);
    }

    /*
     * Called when user enters text into the 'email filter'.
     */
    function emailFilterUpdated(emailFilter) {
      vm.emailFilter = emailFilter;
      vm.pagerOptions.page = 1;
      vm.updateItems.call(vm);
    }

    function getFilters() {
      var filters = vm.superGetFilters();
      if (vm.emailFilter !== '') {
        filters.push('email:like:' + vm.emailFilter);
      }
      return filters;
    }

    function filtersCleared() {
      vm.emailFilter = '';
      vm.superFiltersCleared();
    }
  }

  return component;
});
