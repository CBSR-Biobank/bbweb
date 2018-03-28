/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 * Utility functions to create filter expressions to use with the server's REST API.
 *
 * @memberOf common.services
 */
class FilterExpressionService {

  /**
   * Converts an object to a filter expression that can be used by the REST API.
   *
   * TODO: allow operators to be used (only equals operator is used at the moment)
   *
   * @param {object} element
   *
   * @param {string} element.key - the attribute to filter.
   *
   * @param {string} element.value - the value filter on.
   */
  create(elements) {
    return elements
      .filter(element => !_.isNil(element.value) && (element.value !== ''))
      .map(element => element.key + '::' + element.value)
      .join(';');
  }

}

export default ngModule => ngModule.service('filterExpression', FilterExpressionService)
