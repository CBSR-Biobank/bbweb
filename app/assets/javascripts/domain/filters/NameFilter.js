/**
 *
 */

/* @ngInject */
function NameFilterFactory(SearchFilter) {

  /**
   * NameFilter's aid in using the search API provided by the Biobank REST API.
   */
  function NameFilter() {
    SearchFilter.call(this, NameFilter.name);
  }

  NameFilter.prototype = Object.create(SearchFilter.prototype);
  NameFilter.prototype.constructor = NameFilter;

  NameFilter.prototype.getValue = function () {
    if (this.value !== '') {
      return 'name:like:' + this.value;
    }
    return '';
  };

  return NameFilter;
}

export default ngModule => ngModule.factory('NameFilter', NameFilterFactory)
