/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  filterExpressionService.$inject = [];

  /**
   * Description
   */
  function filterExpressionService() {
    var service = {
      create: create
    };
    return service;

    //-------

    /**
     * Converts an object to a filter expression that can be used by the REST API.
     *
     * TODO: allow operators to be used (only equals operator is used at the moment)
     */
    function create(elements) {
      return _(elements)
        .filter(function (element) { return !_.isUndefined(element.value) && (element.value !== ''); })
        .map(function (element) { return element.key + '::' + element.value; })
        .value()
        .join(';');
    }

  }

  return filterExpressionService;
});
