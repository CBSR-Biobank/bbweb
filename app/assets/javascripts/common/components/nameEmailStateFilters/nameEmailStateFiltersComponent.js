/**
 * AngularJS component available to the rest of the application.
 *
 * @namespace common.components.nameEmailStateFilters
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { NameAndStateFiltersController } from '../../controllers/NameAndStateFiltersController';

/*
 * Controller for this component.
 */
class NameEmailStateFiltersController extends NameAndStateFiltersController {

  $onInit() {
    this.emailFilter = '';
  }

  emailFilterUpdated() {
    this.onEmailFilterUpdated()(this.emailFilter);
  }

  clearFilters() {
    super.clearFilters();
    this.emailFilter = '';
  }
}

/**
 * An AngularJS component that allows the user to enter a value to filter by name, select a state from
 * a drop down menu, or enter a values to filter by email address.
 *
 * These component is used in conjunction with listing domain entities from the server.
 *
 * @memberOf common.components.nameEmailStateFilters
 *
 * @param {Array<domain.filters.StateChoice>} stateData - the information for the states that can be selected.
 *
 * @param {string} selectedState - the initial state to select in the state drop down.
 *
 * @param {common.components.nameFilter.onNameFilterUpdated} onNameFilterUpdated - the function that is called
 * when the user enters text into the name filter input field.
 *
 * @param {common.components.nameEmailStateFilters.onEmailFilterUpdated} onEmailFilterUpdated - the function
 * that is called when the user enters text into the email filter input field.
 *
 * @param {common.components.nameAndStateFilters.onStateFilterUpdated} onStateFilterUpdated - the function
 * that is called when the user selects a state from the dropdown menu.
 *
 * @param {function} onFiltersCleared - the function that is called when the user wishes to clear the values
 * in the name filter input and the state dropdown.
 */
const nameEmailStateFiltersComponent = {
  template: require('./nameEmailStateFilters.html'),
  controller: NameEmailStateFiltersController,
  controllerAs: 'vm',
  bindings: {
    stateData:            '<',
    selectedState:        '@',
    onNameFilterUpdated:  '&',
    onEmailFilterUpdated: '&',
    onStateFilterUpdated: '&',
    onFiltersCleared:     '&'
  }
};

/**
 * The callback function used by {@link common.components.nameEmailStateFilters.nameEmailStateFiltersComponent
 * nameEmailStateFiltersComponent} when the user has entered text into the email address filter input field.
 *
 * @callback common.components.nameEmailStateFilters.onEmailFilterUpdated
 *
 * @param {string} value - the value entered by the user.
 *
 * @returns {undefined}
 */

export default ngModule => ngModule.component('nameEmailStateFilters', nameEmailStateFiltersComponent)
