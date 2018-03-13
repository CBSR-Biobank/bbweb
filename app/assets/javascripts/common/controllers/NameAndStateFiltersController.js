/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * Controller base class used by AngularJS Components that allow the user to filter domain entities when
 * displayed in a table.
 *
 * @memberOf common.controllers
 */
class NameAndStateFiltersController {

  constructor() {
    /**
     * The value to use to filter entities by name.
     *
     * @name common.controllers.NameAndStateFiltersController#nameFilter
     * @type {string}
     */
    this.nameFilter = '';

    /**
     * The value to use to filter entities by state.
     *
     * @name common.controllers.NameAndStateFiltersController#selectedState
     * @type {string}
     * @default all
     */
    this.selectedState = 'all';
  }

  $onInit() {
    this.selectedState = this.selectedState || 'all';
  }

  /**
   * This function is called when the name filter is updated with a new value.
   *
   * @abstract
   */
  onNameFilterUpdated() {
  }

  /**
   * This function is called when the values of all filters are cleared.
   *
   * @abstract
   */
  onFiltersCleared() {
  }

  /**
   * This function is called when the state filter is updated with a new value.
   *
   * @abstract
   */
  onStateFilterUpdated() {
  }

  nameFilterUpdated(value) {
    this.nameFilter = value;
    this.onNameFilterUpdated()(this.nameFilter);
  }

  stateFilterUpdated() {
    this.onStateFilterUpdated()(this.selectedState);
  }

  clearFilters() {
    this.nameFilter = '';
    this.selectedState = 'all';
    this.onFiltersCleared()();
  }
}

// this controller is a base class does not need to be included in AngularJS since it is imported by the
// controllers that extend it
export { NameAndStateFiltersController }
export default () => {}
