/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';
import angular from 'angular'

/**
 * Base class for controllers that display items in a paged fashion.
 *
 * Allows items to be filtered by name and / or state. Additional filters can be used by defining
 * <code>this.getAdditionalFilters</code> and <code>this.clearAdditionalFilters</code>.
 *
 * @param {AngularJs_Service} $log - the service that provides logging functionality.
 *
 * @param {object} $state - the Angular UI Router state object.
 *
 * @param {AngularJs_Service} gettextCatalog - the service that provides string translations functions.
 *
 * @param {object} filters - the filters used by the derived class. See {@link domain.ui.filter|filter}.
 *
 * @param {integer} limit - the number of items to fetch per page.
 *
 * @param {function} this.getItems - A function that returns a promise of PagedResult to display on a single
 * page.
 *
 * @param {object} this.counts - The entity counts. The 'total' field in this object holds the count
 *        of the total number of objects on the server.
 *
 * @param {} this.filters - an array of filters the derived class supports.
 *
 * @return {object} The base class object.
 */
class PagedListController {

  constructor($log,
              $state,
              gettextCatalog,
              filters,
              sortFieldData,
              limit) {
    Object.assign(this, {
      $log,
      $state,
      gettextCatalog,
      filters,
      sortFieldData,
      limit
    })

    this.pagedResult   = { total: 0 };
    this.selectedState = 'all';

    this.pagerOptions = {
      filter: '',
      sort:   'name', // must be lower case
      page:   1,
      limit:  this.limit
    };

    this.displayStates = {
      NO_ENTITIES: 0,
      NO_RESULTS: 1,
      HAVE_RESULTS: 2
    };

    this.displayState = this.determineDisplayState();
    this.onFiltersCleared = this.filtersCleared.bind(this);
  }

  //---

  $onInit() {
    this.updateItems();
  }

  determineDisplayState() {
    if (this.counts && (this.counts.total > 0) && this.pagedResult) {
      if (this.pagedResult.total > 0) {
        return this.displayStates.HAVE_RESULTS;
      } else {
        return this.displayStates.NO_RESULTS;
      }
    }
    return this.displayStates.NO_ENTITIES;
  }

  updateItems() {
    var filters = this.getFilters();
    _.extend(this.pagerOptions, { filter: filters.join(';') });

    this.getItems(this.pagerOptions)
      .then((pagedResult) => {
        if (!pagedResult) { return; }

        this.pagedResult = pagedResult;
        this.pagedResult.items.forEach((entity) => {
          entity.icon = this.getItemIcon(entity);
        });
        this.displayState = this.determineDisplayState();
      })
      .catch((error) => {
        this.$log.error(error);
      });
  }

  /*
   * Returns a function that updates the filter.
   */
  updateSearchFilter(name) {
    var filter = this.filters[name];
    if (_.isUndefined(filter)) {
      throw new Error('filter never assigned: ' + name);
    }
    return (value) => {
      filter.setValue(value);
      this.pagerOptions.page = 1;
      this.updateItems();
    };
  }

  getFilters() {
    return _.values(this.filters)
      .map((filter) => filter.getValue())
      .filter((value) => value !== '');
  }

  sortFieldSelected(sortField) {
    this.pagerOptions.page = 1;
    this.pagerOptions.sort = sortField;
    this.updateItems();
  }

  pageChanged() {
    this.updateItems();
  }

  filtersCleared() {
    _.values(this.filters).forEach((filter) => {
      filter.clearValue();
    });
    this.updateItems();
  }

}

export { PagedListController }

// this controller does not need to be included in AngularJS since it is imported by the controllers that
// extend it
export default () => angular.noop
