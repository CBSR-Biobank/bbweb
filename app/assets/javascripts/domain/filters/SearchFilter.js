/**
 *
 */
define(function () {
  'use strict';

  SearchFilterFactory.$inject = [];

  function SearchFilterFactory() {

    /**
     * SearchFilter's aid in using the search API provided by the Biobank REST API.
     */
    function SearchFilter(name) {

      /**
       * A short identifying name.
       *
       * @name domain.SearchFilter#name
       * @type {string}
       */
      this.name = name;

      /**
       * A short identifying name that is unique.
       *
       * @name domain.SearchFilter#value
       * @type {string}
       */
      this.value = '';
    }

    SearchFilter.prototype.setValue = function (value) {
      this.value = value;
    };

    SearchFilter.prototype.getValue = function () {
      return this.value;
    };

    SearchFilter.prototype.clearValue = function () {
      this.setValue('');
    };

    return SearchFilter;
  }

  return SearchFilterFactory;
});
