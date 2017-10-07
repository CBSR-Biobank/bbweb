/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
class Controller {
  constructor($controller) {
    'ngInject';
    $controller('NameAndStateFiltersController', { vm: this });
  }

}

const component = {
  template: require('./nameAndStateFilters.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
    stateData:            '<',
    onNameFilterUpdated:  '&',
    onStateFilterUpdated: '&',
    onFiltersCleared:     '&'
  }
};

export default component;
