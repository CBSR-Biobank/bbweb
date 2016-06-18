/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  /**
   * Displays items in a panel list. Can only be used for studies and centres.
   */
  function pagedItemsListDirective() {
    var directive = {
      restrict: 'E',
      scope:{},
      bindToController: {
        counts:                       '=',
        pageSize:                     '=',
        possibleStatuses:             '=',
        messageNoItems:               '@',
        messageNoResults:             '@',
        getItems:                     '&',
        getItemIcon:                  '&',
        entityNavigateState:          '@',
        entityNavigateStateParamName: '@'
      },
      templateUrl: '/assets/javascripts/common/directives/pagedItemsList/pagedItemsList.html',
      controller: PagedItemsListCtrl,
      controllerAs: 'vm'
    };

    return directive;
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
      status:     vm.possibleStatuses[0].id,
      page:       1,
      pageSize:   vm.pageSize,
      sort:       'name' // must be lower case
    };

    vm.displayStates = {
      NO_ENTITIES: 0,
      NO_RESULTS: 1,
      HAVE_RESULTS: 2
    };

    vm.displayState = displayState();
    vm.panelHeading = panelHeading(vm.counts);

    updateItems();

    $scope.$watch('vm.counts', function () {
      vm.panelHeading = panelHeading(vm.counts);
    });

    //---

    // TODO change this to a directive?
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
        vm.pagedResult.items = _.map(vm.pagedResult.items, function (entity) {
          entity.icon = vm.getItemIcon()(entity);
          return entity;
        });
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
      vm.pagerOptions.page = 1;
      vm.pagerOptions.sort = sortField.toLowerCase(); // must be lower case
      updateItems();
    }
  }

  return pagedItemsListDirective;
});
