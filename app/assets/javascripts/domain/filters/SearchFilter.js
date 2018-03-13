/**
 *
 */

/* @ngInject */
function SearchFilterFactory() {

  /**
   * SearchFilter's aid in using the search API provided by the Biobank REST API.
   *
   * @memberOf domain.filters
   */
  class SearchFilter {

    /**
     * @param {string} name - the name of the filter.
     */
    constructor(name) {

      /**
       * A short identifying name.
       *
       * @name domain.SearchFilter#name
       * @type {string}
       */
      this.name = name;

      /**
       * The value to use for this filter.
       *
       * @name domain.SearchFilter#value
       * @type {string}
       */
      this.value = '';
    }

    setValue(value) {
      this.value = value;
    }

    getValue() {
      return this.value;
    }

    clearValue() {
      this.setValue('');
    }
  }

  return SearchFilter;
}

export default ngModule => ngModule.factory('SearchFilter', SearchFilterFactory)
