define(['../module', 'underscore'], function(module, _) {
  'use strict';

  module.service('domainEntityService', domainEntityService);

  //domainEntityService.$inject = [];

  /**
   * Utilities for services that access domain objects.
   */
  function domainEntityService() {
    var service = {
      getOptionalAttribute: getOptionalAttribute
    };
    return service;

    /**
     * Returns an object with the attribute set if it is not null or has length > 0 in "obj".
     */
    function getOptionalAttribute(obj, attribute) {
      var result = {};
      if (obj[attribute] && (obj[attribute].length > 0)) {
        if (obj[attribute] instanceof Array) {
          var arr = _.compact(obj[attribute]);
          if (arr.length > 0) {
            result[attribute] = arr;
          }
        } else {
          result[attribute] = obj[attribute];
        }
      }
      return result;
    }

  }

});
