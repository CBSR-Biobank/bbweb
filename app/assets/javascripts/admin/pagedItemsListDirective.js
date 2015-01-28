define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.directive('pagedItemsList', pagedItemsList);

  /**
   * Displays items in a panel list. Can only be used for studies and centres.
   */
  function pagedItemsList() {
    var directive = {
      restrict: 'E',
      scope:{
        counts:           '=',
        pageSize:         '=',
        possibleStatuses: '=',
        messageNoItems:   '@',
        messageNoResults: '@',
        getItems:         '&'
      },
      templateUrl: '/assets/javascripts/admin/pagedItemsList.html',
      controller: 'ItemPagedListCtr as vm'
    };
    return directive;
  }

  module.controller('ItemPagedListCtr', ItemPagedListCtr);

  ItemPagedListCtr.$inject = ['$scope'];

  function ItemPagedListCtr($scope) {
    var vm = this;

    vm.counts              = $scope.counts;
    vm.possibleStatuses    = $scope.possibleStatuses;
    vm.getItems            = $scope.getItems;
    vm.messageNoItems      = $scope.messageNoItems;
    vm.messageNoResults    = $scope.messageNoResults;
    vm.pagedResult         = { total: vm.counts.total };
    vm.paginationNumPages  = 5;
    vm.sortFields          = ['Name', 'Status'];
    vm.nameFilterUpdated   = nameFilterUpdated;
    vm.statusFilterUpdated = statusFilterUpdated;
    vm.pageChanged         = pageChanged;
    vm.sortFieldSelected   = sortFieldSelected;
    vm.clearFilters        = clearFiters;

    vm.pagerOptions = {
      filter:     '',
      status:     $scope.possibleStatuses[0],
      page:       1,
      pageSize:   $scope.pageSize,
      sortField:  'name' // must be lower case
    };

    vm.displayStates = {
      NO_STUDIES: 0,
      NO_RESULTS: 1,
      HAVE_RESULTS: 2
    };

    vm.displayState = displayState();
    vm.panelHeading = panelHeading(vm.counts);

    updateItems();

    //---

    function panelHeading(counts) {
      var panelHeadingItems = [
        { label: 'Disabled', value: counts.disabledCount, icon:  'glyphicon-cog' },
        { label: 'Enabled', value: counts.enabledCount, icon:  'glyphicon-ok-circle' }
      ];

      if (counts.retiredCount !== undefined) {
        panelHeadingItems.push(
          { label: 'Retired', value: vm.counts.retiredCount, icon:  'glyphicon-remove-sign' });
      }

      return _.map(panelHeadingItems, function (item) {
        var iconHtml = !!item.icon ? '<i class="glyphicon ' + item.icon + '"></i> ' : '';
        return iconHtml + item.value + ' ' + item.label;
      }).join(', ');
    }

    function displayState() {
      if ((vm.counts.total > 0) && vm.pagedResult) {
        if (vm.pagedResult.total > 0) {
          return vm.displayStates.HAVE_RESULTS;
        } else {
          return vm.displayStates.NO_RESULTS;
        }
      } else {
        return vm.displayStates.NO_STUDIES;
      }
    }

    function updateItems() {
      console.log('updateItems', vm.pagerOptions);
      vm.getItems()(vm.pagerOptions).then(function (pagedResult) {
        vm.pagedResult = pagedResult;
        vm.displayState = displayState();
      });
    }

    /**
     * Called when user enters text into the 'name filter'.
     */
    function nameFilterUpdated() {
      vm.pagerOptions.page = 1;
      updateItems();
    }

    /**
     * Called when user selects a status from the 'status filter' select.
     */
    function statusFilterUpdated() {
      vm.pagerOptions.page = 1;
      updateItems();
    }

    function pageChanged() {
      updateItems();
    }

    function clearFiters() {
      vm.pagerOptions.filter = null;
      vm.pagerOptions.status.id = 'all';
      updateItems();
    }

    function sortFieldSelected(sortField) {
      vm.pagerOptions.sortField = sortField.toLowerCase(); // must be lower case
      updateItems();
    }
  }

});
