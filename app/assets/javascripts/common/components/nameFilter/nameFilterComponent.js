/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import { NameAndStateFiltersController } from '../../controllers/NameAndStateFiltersController';

/*
 * Controller for this component.
 */
class Controller extends NameAndStateFiltersController {}

const component = {
  template: require('./nameFilter.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
    onNameFilterUpdated:  '&',
    onFiltersCleared:     '&'
  }
};

export default ngModule => ngModule.component('nameFilter', component)
