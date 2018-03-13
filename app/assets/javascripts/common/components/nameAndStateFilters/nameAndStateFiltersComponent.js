/**
 * AngularJS component available to the rest of the application.
 *
 * @namespace common.components.nameAndStateFilters
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { NameAndStateFiltersController } from '../../controllers/NameAndStateFiltersController';

/**
 * An AngularJS component that allows the user to enter a value to filter by name and select a state from
 * a drop down menu.
 *
 * These component is used in conjunction with listing domain entities from the server.
 *
 * @memberOf common.components.nameAndStateFilters
 *
 * @param {Array<domain.filters.StateChoice>} stateData - the information for the states that can be selected.
 *
 * @param {string} selectedState=all - the initial state to select in the state drop down.
 *
 * @param {common.components.nameFilter.onNameFilterUpdated} onNameFilterUpdated - the function that is called
 * when the user enters text into the name filter input field.
 *
 * @param {common.components.nameAndStateFilters.onStateFilterUpdated} onStateFilterUpdated - the function
 * that is called when the user selects a state from the dropdown menu.
 *
 * @param {function} onFiltersCleared - the function that is called when the user wishes to clear the values
 * in the name filter input and the state dropdown.
 */
const nameAndStateFiltersComponent = {
  template: require('./nameAndStateFilters.html'),
  controller: NameAndStateFiltersController,
  controllerAs: 'vm',
  bindings: {
    stateData:            '<',
    selectedState:        '@',
    onNameFilterUpdated:  '&',
    onStateFilterUpdated: '&',
    onFiltersCleared:     '&'
  }
};

/**
 * The callback function used by {@link common.components.nameAndStateFilters.nameAndStateFiltersComponent
 * nameAndStateFiltersComponent} when the user selects a state from the dropdown menu.
 *
 * @callback common.components.nameAndStateFilters.onStateFilterUpdated
 *
 * @param {string} id - the states ID.
 *
 * @returns {undefined}
 */

export default ngModule => ngModule.component('nameAndStateFilters', nameAndStateFiltersComponent)
