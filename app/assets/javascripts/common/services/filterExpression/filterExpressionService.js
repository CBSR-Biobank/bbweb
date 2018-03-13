/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

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
    return elements
      .filter(element => !_.isNil(element.value) && (element.value !== ''))
      .map(element => element.key + '::' + element.value)
      .join(';');
  }

}

export default ngModule => ngModule.service('filterExpression', filterExpressionService)
