/**
 * AngularJS component available to the rest of the application.
 *
 * @namespace common.components.nameFilter
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { NameAndStateFiltersController } from '../../controllers/NameAndStateFiltersController';

/**
 * An AngularJS component that allows the user to enter a value to filter by name.
 *
 * This component is used in conjunction with listing domain entities from the server.
 *
 * @memberOf common.components.nameFilter
 *
 * @param {common.components.nameFilter.onNameFilterUpdated} onNameFilterUpdated - the function that is called
 * when the user enters text into the name filter input field.
 *
 * @param {function} onFiltersCleared - the function that is called when the user wishes to clear the values
 * in the name filter input and the state dropdown.
 */
const nameFilterComponent = {
  template: require('./nameFilter.html'),
  controller: NameAndStateFiltersController,
  controllerAs: 'vm',
  bindings: {
    onNameFilterUpdated:  '&',
    onFiltersCleared:     '&'
  }
};

/**
 * The callback function used by {@link common.components.nameAndStateFilters.nameAndStateFiltersComponent
 * nameAndStateFiltersComponent} when the user has entered text into the name filter input field.
 *
 * @callback common.components.nameFilter.onNameFilterUpdated
 *
 * @param {string} value - the value entered by the user.
 *
 * @returns {undefined}
 */

export default ngModule => ngModule.component('nameFilter', nameFilterComponent)
