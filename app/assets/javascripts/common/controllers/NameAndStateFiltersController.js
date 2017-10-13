/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller base class used by nameAndStateFiltersComponent and nameEmailStateFiltersComponent.
 */
export default class NameAndStateFiltersController {

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
