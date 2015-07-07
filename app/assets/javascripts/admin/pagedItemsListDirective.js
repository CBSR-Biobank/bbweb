/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  /**
   * Displays items in a panel list. Can only be used for studies and centres.
   */
  function pagedItemsListDirective() {
    return {
      restrict: 'E',
      scope:{
        counts:                       '=',
        pageSize:                     '=',
        possibleStatuses:             '=',
        messageNoItems:               '@',
        messageNoResults:             '@',
        getItems:                     '&',
        entityNavigateState:          '@',
        entityNavigateStateParamName: '@'
      },
      templateUrl: '/assets/javascripts/admin/pagedItemsList.html',
      controller: 'PagedItemsListCtrl as vm'
    };
  }

  PagedItemsListCtrl.$inject = ['$scope'];

  /**
   * @param {Array} $scope.possibleStatuses - an array with 2 keys: 'id' and 'name'. The value for the 'id'
   * key is a status for the entities being displayed, and used with the $scope.getItems function. The value
   * for the 'Name' key is what is displayed in the 'Status' drop down box. The first item of the array should be
   * { id: 'all', name: 'All' } so that all items are displayed.
   */
  function PagedItemsListCtrl($scope) {
    var vm = this;

    vm.counts                       = $scope.counts;
    vm.possibleStatuses             = $scope.possibleStatuses;
    vm.getItems                     = $scope.getItems;
    vm.entityNavigateState          = $scope.entityNavigateState;
    vm.entityNavigateStateParamName = $scope.entityNavigateStateParamName;
    vm.messageNoItems               = $scope.messageNoItems;
    vm.messageNoResults             = $scope.messageNoResults;
    vm.pagedResult                  = { total: vm.counts.total };
    vm.paginationNumPages           = 5;
    vm.sortFields                   = ['Name', 'Status'];
    vm.nameFilterUpdated            = nameFilterUpdated;
    vm.statusFilterUpdated          = statusFilterUpdated;
    vm.pageChanged                  = pageChanged;
    vm.sortFieldSelected            = sortFieldSelected;
    vm.clearFilters                 = clearFilters;

    vm.pagerOptions = {
      filter:     '',
      status:     $scope.possibleStatuses[0],
      page:       1,
      pageSize:   $scope.pageSize,
      sortField:  'name' // must be lower case
    };

    vm.displayStates = {
      NO_ENTITIES: 0,
      NO_RESULTS: 1,
      HAVE_RESULTS: 2
    };

    vm.displayState = displayState();
    vm.panelHeading = panelHeading(vm.counts);

    updateItems();

    //---

    // FIXME change this to a directive?
    function panelHeading(counts) {
      var panelHeadingItems = [
        { label: 'Disabled', value: counts.disabled, icon:  'glyphicon-cog' },
        { label: 'Enabled', value: counts.enabled, icon:  'glyphicon-ok-circle' }
      ];

      if (!_.isUndefined(counts.retired)) {
        panelHeadingItems.push(
          { label: 'Retired', value: vm.counts.retired, icon:  'glyphicon-remove-sign' });
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
        return vm.displayStates.NO_ENTITIES;
      }
    }

    function updateItems() {
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

    function clearFilters() {
      vm.pagerOptions.filter = null;
      vm.pagerOptions.status = vm.possibleStatuses[0];
      updateItems();
    }

    function sortFieldSelected(sortField) {
      vm.pagerOptions.sortField = sortField.toLowerCase(); // must be lower case
      updateItems();
    }
  }

  return {
    directive: pagedItemsListDirective,
    controller: PagedItemsListCtrl
  };
});
