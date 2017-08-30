/**
 *
 */
define(function () {
  'use strict';

  EmailFilterFactory.$inject = [
    'SearchFilter'
  ];

  function EmailFilterFactory(SearchFilter) {

    /**
     * EmailFilter's aid in using the search API provided by the Biobank REST API.
     */
    function EmailFilter() {
      SearchFilter.call(this, EmailFilter.name);
    }

    EmailFilter.prototype = Object.create(SearchFilter.prototype);
    EmailFilter.prototype.constructor = EmailFilter;

    EmailFilter.prototype.getValue = function () {
      if (this.value !== '') {
        return 'email:like:' + this.value;
      }
      return '';
    };

    return EmailFilter;
  }

  return EmailFilterFactory;
});
