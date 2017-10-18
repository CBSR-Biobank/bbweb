/**
 *
 */

import _ from 'lodash'

/**
 * Allows for converting enumerations to labels that are displayed on a web page.
 */
function labelService() {
  var service = {
    getLabel: getLabel
  };
  return service;

  //-------

  function getLabel(labels, value) {
    var result = labels[value];
    if (_.isUndefined(result)) {
      throw new Error('no such label: ' + value);
    }
    return result;
  }


}

export default ngModule => ngModule.service('labelService', labelService)
