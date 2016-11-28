/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  /**
   * Displays items in a panel list. Can only be used for collections {@link domain.study.Study} and {@link
   * domain.cnetres.Centres}.
   *
   * @param {Array} possibleStates - an array of objects, where each object has 2 keys: <code>id</code> and
   *        <code>name</code>. The value for the <code>id</code> key is a status for the entities being
   *        displayed, and used with the $scope.getItems function. The value for the <code>name</code> key is
   *        what is displayed in the 'State' drop down box. The first item of the array should be <code>{ id:
   *        'all', name: 'All' }</code> so that all items are displayed.
   */
  function pagedItemsListDirective() {
    var directive = {
      restrict: 'E',
      scope:{},
      bindToController: {
        counts:                       '=',
        limit:                        '=',
        possibleStates:               '=',
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

  PagedItemsListCtrl.$inject = ['$scope', 'gettextCatalog', 'filterExpression'];

  /**
   */
  function PagedItemsListCtrl($scope, gettextCatalog, filterExpression) {
    var vm = this;

    vm.nameFilter         = '';
    vm.nameFilterWildcard = '';
    vm.selectedState      = 'all';
    vm.pagedResult        = { total: vm.counts.total };
    vm.sortFields         = [ gettextCatalog.getString('Name'), gettextCatalog.getString('State') ];
    vm.nameFilterUpdated  = nameFilterUpdated;
    vm.stateFilterUpdated = stateFilterUpdated;
    vm.pageChanged        = pageChanged;
    vm.sortFieldSelected  = sortFieldSelected;
    vm.clearFilters       = clearFilters;

    vm.pagerOptions = {
      filter: '',
      sort:   'name', // must be lower case
      page:   1,
      limit:  vm.limit
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
      var filterElements = [
            { key: 'name',  value: vm.nameFilterWildcard },
            { key: 'state', value: vm.stateFilter }
          ];

      _.extend(vm.pagerOptions, { filter: filterExpression.create(filterElements) });

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
      if (!_.isUndefined(vm.nameFilter) && (vm.nameFilter !== '')) {
        vm.nameFilterWildcard = '*' + vm.nameFilter + '*';
      } else {
        vm.nameFilterWildcard = '';
      }
      vm.pagerOptions.page = 1;
      updateItems();
    }

    /**
     * Called when user selects a state from the 'state filter' select.
     */
    function stateFilterUpdated() {
      vm.stateFilter = (vm.selectedState !== 'all') ? vm.selectedState : '';
      vm.pagerOptions.page = 1;
      updateItems();
    }

    function sortFieldSelected(sortField) {
      vm.pagerOptions.page = 1;
      vm.pagerOptions.sort = sortField.toLowerCase(); // must be lower case
      updateItems();
    }

    function pageChanged() {
      updateItems();
    }

    function clearFilters() {
      vm.nameFilter = '';
      vm.sortSelectedState = 'all';
      updateItems();
    }
  }

  return pagedItemsListDirective;
});
