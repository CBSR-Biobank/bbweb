/* global define */
define(['../module'], function(module) {
  'use strict';

  module.directive('selectStudy', selectStudyDirective);

  /**
   *
   */
  function selectStudyDirective() {
    var directive = {
      restrict: 'E',
      scope: {
        getHeader:              '&',
        getStudies:             '&',
        pageSize:               '=',
        messageNoResults:       '@',
        icon:                   '@',
        navigateStateName:      '@',
        navigateStateParamName: '@'
      },
      templateUrl : '/assets/javascripts/admin/studies/selectStudy.html',
      controller: 'SelectStudyCtr as vm'
    };
    return directive;

  }

  module.controller('SelectStudyCtr', SelectStudyCtr);

  SelectStudyCtr.$inject = ['$scope', '$state'];

  function SelectStudyCtr($scope, $state) {
    var vm = this;

    vm.displayStates = {
      NO_RESULTS: 0,
      HAVE_RESULTS: 1
    };

    vm.getHeader              = $scope.getHeader;
    vm.getStudies             = $scope.getStudies;
    vm.icon                   = $scope.icon;
    vm.navigateStateName      = $scope.navigateStateName;
    vm.navigateStateParamName = $scope.navigateStateParamName;
    vm.pageSize               = 10;
    vm.updateStudies          = updateStudies;
    vm.messageNoResults       = $scope.messageNoResults;
    vm.pageSize               = 10;
    vm.pagedResult            = {};
    vm.paginationNumPages     = 5;
    vm.nameFilterUpdated      = nameFilterUpdated;
    vm.pageChanged            = pageChanged;
    vm.clearFilter            = clearFiter;
    vm.displayState           = getDisplayState();
    vm.navigateToStudyHref    = navigateToStudyHref;
    vm.showPagination         = getShowPagination();

    vm.pagerOptions = {
      filter:    '',
      status:    'enabled',
      page:      1,
      pageSize:  vm.pageSize,
      sortField: 'name' // must be lower case
    };

    updateStudies();

    //---

    function getDisplayState() {
      if (vm.pagedResult.total > 0) {
        return vm.displayStates.HAVE_RESULTS;
      } else {
        return vm.displayStates.NO_RESULTS;
      }
    }

    function updateStudies() {
      vm.getStudies()(vm.pagerOptions).then(function (pagedResult) {
        vm.pagedResult = pagedResult;
        vm.displayState = getDisplayState();
        vm.showPagination = getShowPagination();
      });
    }

    /**
     * Called when user enters text into the 'name filter'.
     */
    function nameFilterUpdated() {
      vm.pagerOptions.page = 1;
      updateStudies();
    }

    function pageChanged() {
      updateStudies();
    }

    function clearFiter() {
      vm.pagerOptions.filter = null;
      updateStudies();
    }

    function navigateToStudyHref(study) {
      if (!!vm.navigateStateName) {
        var stateParam = {};
        stateParam[vm.navigateStateParamName] = study.id;
        var href = $state.href(vm.navigateStateName, stateParam, {absolute: true});
        return '<a href="' + href + '"><strong><i class="glyphicon ' + vm.icon + '"></i> ' +
          study.name + '</strong></a>';
      }

      return study.name;
    }

    function getShowPagination() {
      return (vm.displayState === vm.displayStates.HAVE_RESULTS) &&
        (vm.pagedResult.maxPages > 1);
    }
  }

});
