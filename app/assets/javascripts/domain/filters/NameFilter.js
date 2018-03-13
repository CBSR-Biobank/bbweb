/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function NameFilterFactory(SearchFilter) {

  /**
   * NameFilter's aid in using the search API provided by the Biobank REST API.
   *
   * @memberOf domain.filters
   */
  class NameFilter extends SearchFilter {

    constructor() {
      super(NameFilter.name);
    }

    getValue() {
      if (this.value !== '') {
        return 'name:like:' + this.value;
      }
    return '';
    }
  }

  return NameFilter;
}

export default ngModule => ngModule.factory('NameFilter', NameFilterFactory)
