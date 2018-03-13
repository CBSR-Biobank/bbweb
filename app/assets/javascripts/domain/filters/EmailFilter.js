/**
 *
 */

/* @ngInject */
function EmailFilterFactory(SearchFilter) {

  /**
   * EmailFilter's aid in using the search API provided by the Biobank REST API.
   *
   * @memberOf domain.filters
   */
  class EmailFilter extends SearchFilter {

    constructor() {
      super(EmailFilter.name);
    }

    getValue() {
      if (this.value !== '') {
        return 'email:like:' + this.value;
      }
      return '';
    }

  }

  return EmailFilter;
}

export default ngModule => ngModule.factory('EmailFilter', EmailFilterFactory)
