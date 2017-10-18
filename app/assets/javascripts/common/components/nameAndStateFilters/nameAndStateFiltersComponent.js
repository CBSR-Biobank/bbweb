/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import { NameAndStateFiltersController } from '../../controllers/NameAndStateFiltersController';

const component = {
  template: require('./nameAndStateFilters.html'),
  controller: NameAndStateFiltersController,
  controllerAs: 'vm',
  bindings: {
    stateData:            '<',
    onNameFilterUpdated:  '&',
    onStateFilterUpdated: '&',
    onFiltersCleared:     '&'
  }
};

export default ngModule => ngModule.component('nameAndStateFilters', component)
