/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller base class used by nameAndStateFiltersComponent and nameEmailStateFiltersComponent.
 */
class NameAndStateFiltersController {

  constructor() {
    this.nameFilter = '';
    this.selectedState = 'all';
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

// this controller does not need to be included in AngularJS since it is imported by the controllers that
// extend it
export { NameAndStateFiltersController }
export default () => {}
