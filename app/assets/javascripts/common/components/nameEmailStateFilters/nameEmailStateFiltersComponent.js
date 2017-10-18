/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import { NameAndStateFiltersController } from '../../controllers/NameAndStateFiltersController';

/*
 * Controller for this component.
 */
class Controller extends NameAndStateFiltersController {

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

const component = {
  template: require('./nameEmailStateFilters.html'),
  controller: Controller,
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

export default ngModule => ngModule.component('nameEmailStateFilters', component)
