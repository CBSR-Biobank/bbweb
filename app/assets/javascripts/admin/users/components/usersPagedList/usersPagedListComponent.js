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
    controller: Controller,
    controllerAs: 'vm',
    bindings: {
    }
  };

  Controller.$inject = [
    '$controller',
    '$log',
    '$scope',
    '$state',
    'User',
    'UserState',
    'userStateLabelService',
    'UserCounts',
    'gettextCatalog',
    'EmailFilter',
    'NameFilter',
    'StateFilter',
  ];

  /*
   * Controller for this component.
   */
  function Controller($controller,
                      $log,
                      $scope,
                      $state,
                      User,
                      UserState,
                      userStateLabelService,
                      UserCounts,
                      gettextCatalog,
                      EmailFilter,
                      NameFilter,
                      StateFilter) {
    var vm = this,
        stateData = _.values(UserState).map(function (state) {
          return { id: state, label: userStateLabelService.stateToLabelFunc(state) };
        });

    vm.$onInit = onInit;
    vm.filters = {};
    vm.filters[NameFilter.name]  = new NameFilter();
    vm.filters[EmailFilter.name] = new EmailFilter();
    vm.filters[StateFilter.name] = new StateFilter(true, stateData, 'all');

    //--

    function onInit() {
      vm.emailFilter          = '';
      vm.counts               = {};
      vm.limit                = 5;
      vm.getItems             = getItems;
      vm.getItemIcon          = getItemIcon;

      // initialize this controller's base class
      $controller('PagedListController', {
        vm:             vm,
        $log:           $log,
        $state:         $state,
        gettextCatalog: gettextCatalog
      });

      vm.stateLabelFuncs = {};
       _.values(UserState).forEach(function (state) {
        vm.stateLabelFuncs[state] = userStateLabelService.stateToLabelFunc(state);
      });

      vm.sortFieldData = [
        { id: 'name',  labelFunc: function () {  return gettextCatalog.getString('Name'); } },
        { id: 'email', labelFunc: function () {  return gettextCatalog.getString('Email'); } },
        { id: 'state', labelFunc: function () {  return gettextCatalog.getString('State'); } }
      ];

      UserCounts.get()
        .then(function (counts) {
          vm.userCounts = counts;
          vm.haveUsers  = (vm.userCounts.total > 0);
        })
        .catch(function (error) {
          $log.error(error);
        });
    }

    function getItems(options) {
      return UserCounts.get()
        .then(function (counts) {
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
  }

  return component;
});
