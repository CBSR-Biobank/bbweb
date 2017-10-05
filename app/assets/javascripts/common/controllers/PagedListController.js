/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  PagedListController.$inject = ['vm', '$log', '$state', 'gettextCatalog'];

  /**
   * Base class for controllers that display items in a paged fashion.
   *
   * Allows items to be filtered by name and / or state. Additional filters can be used by defining
   * <code>vm.getAdditionalFilters</code> and <code>vm.clearAdditionalFilters</code>.
   *
   * @param {object} vm - The derived controller object.
   *
   * @param {function} vm.getItems - A function that returns a promise of PagedResult to display on a single
   * page.
   *
   * @param {object} vm.counts - The entity counts. The 'total' field in this object holds the count
   *        of the total number of objects on the server.
   *
   * @param {string[]} vm.stateData - an array of objects, used to filter entities by state. Each object has 2
   *        keys: <code>id</code> and <code>name</code>. The value for the <code>id</code> key is a state for
   *        the entities being displayed, and used with the $scope.getItems function. The value for the
   *        <code>name</code> key is what is displayed in the 'State' drop down box.
   *
   * @param {domain.ui.filter[]} vm.filters - an array of filters the derived class supports.
   *
   * @param {function|undefined} vm.onFiltersCleared - a function to be called when all filters are cleared.
   *
   * @param {function|undefined} vm.handleUnauthorized - the function to gall if vm.getItems returns
   *        a rejected promise. If this function is not defined, then the state will be changed to
   *        the login page on a rejected promise.
   *
   * @param {object} $state - the Angular UI Router state object.
   *
   * @param {AngularJs_Service} gettextCatalog - the service that provides string translations functions.
   *
   * @return {object} The base class object.
   */
  function PagedListController(vm, $log, $state, gettextCatalog) {
    vm.pagedResult        = { total: 0 };
    vm.selectedState      = 'all';
    vm.pageChanged        = pageChanged;
    vm.sortFieldSelected  = sortFieldSelected;
    vm.updateSearchFilter = updateSearchFilter;
    vm.filtersCleared     = filtersCleared;
    vm.getFilters         = getFilters;
    vm.updateItems        = updateItems;

    vm.sortFieldData         = [
      { id: 'name',  labelFunc: function () {  return gettextCatalog.getString('Name'); } },
      { id: 'state', labelFunc: function () {  return gettextCatalog.getString('State'); } }
    ];

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
    updateItems();

    //---

    function displayState() {
      if (vm.counts && (vm.counts.total > 0) && vm.pagedResult) {
        if (vm.pagedResult.total > 0) {
          return vm.displayStates.HAVE_RESULTS;
        } else {
          return vm.displayStates.NO_RESULTS;
        }
      }
      return vm.displayStates.NO_ENTITIES;
    }

    function updateItems() {
      var filters = vm.getFilters();
      _.extend(vm.pagerOptions, { filter: filters.join(';') });

      vm.getItems(vm.pagerOptions)
        .then(function (pagedResult) {
          if (!pagedResult) { return; }

          vm.pagedResult = pagedResult;
          vm.pagedResult.items.forEach((entity) => {
            entity.icon = vm.getItemIcon(entity);
          });
          vm.displayState = displayState();
        })
        .catch(function (error) {
          $log.error(error);
        });
    }

    /*
     * Returns a function that updates the filter.
     */
    function updateSearchFilter(name) {
      var filter = vm.filters[name];
      if (_.isUndefined(filter)) {
        throw new Error('filter never assigned: ' + name);
      }
      return function (value) {
        filter.setValue(value);
        vm.pagerOptions.page = 1;
        updateItems();
      };
    }

    function getFilters() {
      return _.values(vm.filters)
        .map(function (filter) {
          return filter.getValue();
        })
        .filter(function (value) {
          return value !== '';
        });
    }

    function sortFieldSelected(sortField) {
      vm.pagerOptions.page = 1;
      vm.pagerOptions.sort = sortField;
      updateItems();
    }

    function pageChanged() {
      updateItems();
    }

    function filtersCleared() {
      _.values(vm.filters).forEach(function (filter) {
        filter.clearValue();
      });
      updateItems();
      if (!_.isNil(vm.onFiltersCleared)) {
        vm.onFiltersCleared();
      }
    }

  }

  return PagedListController;
});
