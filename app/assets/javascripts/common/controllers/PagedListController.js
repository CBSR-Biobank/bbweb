/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';
import angular from 'angular'

const DisplayStates = {
  NO_ENTITIES:  0,
  NO_RESULTS:   1,
  HAVE_RESULTS: 2
};

/**
 * Base class for controllers that display items in a paged fashion.
 *
 * Allows items to be filtered by different attributes (e.g.name and / or state).
 *
 * @memberOf common.controllers
 */
class PagedListController {

  /**
   *
   * @param {AngularJs_Service} $log
   *
   * @param {AngularJs_Service} $state
   *
   * @param {AngularJs_Service} gettextCatalog
   *
   * @param {AngularJs_Service} resourceErrorService
   *
   * @param {Array<domain.filters.SearchFilter>} filters - the filters used by the derived class.
   *
   * @param {Array<common.controllers.PagedListController.SortChoice>} sortChoices - the sort fields the
   * data can be sorted by.
   *
   * @param {integer} limit - the number of items to fetch per page.
   */
  constructor($log,
              $state,
              gettextCatalog,
              resourceErrorService,
              filters,
              sortChoices,
              limit) {
    /**
     * The entity counts indexed by state. The 'total' field in this object holds the count of the total
     * number of objects on the server.
     *
     * @name common.controllers.PagedListController#counts
     * @type {string}
     */

    /**
     * An array of filters the derived class supports.
     *
     */

    Object.assign(this, {
      $log,
      $state,
      gettextCatalog,
      resourceErrorService,
      filters,
      sortChoices,
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

    this.displayState = this.determineDisplayState();
    this.onFiltersCleared = this.filtersCleared.bind(this);
  }

  $onInit() {
    this.updateItems();
  }

  determineDisplayState() {
    if (this.counts && (this.counts.total > 0) && this.pagedResult) {
      if (this.pagedResult.total > 0) {
        return DisplayStates.HAVE_RESULTS;
      } else {
        return DisplayStates.NO_RESULTS;
      }
    }
    return DisplayStates.NO_ENTITIES;
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
      .catch(this.resourceErrorService.checkUnauthorized());
  }

  /**
   * Retrieves data from the server.
   *
   * @abstract
   * @returns {Promise<common.controllers.PagedListController.PagedResult>} A promise with items of the
   * type used by the derived class.
   */
  getItems() {
    throw new Error('derived class should override this method')
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

  hasResultsToDisplay() {
    return this.displayState === DisplayStates.HAVE_RESULTS;
  }

  hasNoEntitiesToDisplay() {
    return this.displayState === DisplayStates.NO_ENTITIES;
  }

  hasNoResultsToDisplay() {
    return this.displayState === DisplayStates.NO_RESULTS;
  }

}

/**
 * Object returned by server for a paged API call.
 *
 * @typedef common.controllers.PagedListController.PagedResult
 *
 * @type object
 *
 * @property {Array<domain.ConcurrencySafeEntity>} id - the items in the result set.
 *
 * @property {int} page - The page these results correspond to.
 *
 * @property {int} limit - The number of items in this result set.
 *
 * @property {int} offset - The page offset. Starts at 0.
 *
 * @property {int} total - the total elements in all pages.
 */

/**
 * @typedef common.controllers.PagedListController.SortChoice
 * @type object
 *
 * @property {string} id - the ID to used to identify the state.
 *
 * @property {common.controllers.PagedListController.LabelFunc} label - the function to call that returns
 * a label that is displayed to the user.
 */

/**
 * The callback function called by {@link common.controllers.PagedListController.SortChoice SortChoice}
 * that returns a label to show to the user.
 *
 * @callback common.controllers.PagedListController.LabelFunc
 *
 * @param {string} stateId - the ID of the state.
 */

/**
 * Object used to request paged results.
 *
 * @typedef common.controllers.PagedListController.PagerOptions
 *
 * @property {string} [filter=''] The filter to use on entity names. Default is empty string.
 *
 * @property {string} [sort] The field to sort the entities by.
 *
 * @property {int} [page=1] If the total results are longer than limit, then page selects which
 * entities should be returned. If an invalid value is used then the response is an error.
 *
 * @property {int} [limit] The total number of entities to return per page.
 */

export { PagedListController }

// this controller does not need to be included in AngularJS since it is imported by the controllers that
// extend it
export default () => angular.noop
