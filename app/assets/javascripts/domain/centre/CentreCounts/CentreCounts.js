/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/**
 *
 */
/* @ngInject */
function CentreCountsFactory(biobankApi) {

  function CentreCounts(options = {}) {
    var defaults = {
      total:    0,
      disabled: 0,
      enabled:  0
    };

    Object.assign(this, defaults, _.pick(options, Object.keys(defaults)));
  }

  CentreCounts.get = function () {
    return biobankApi.get('/api/centres/counts')
      .then(response => new CentreCounts({
        total:    response.total,
        disabled: response.disabledCount,
        enabled:  response.enabledCount
      }))
  };

  return CentreCounts;
}

export default ngModule => ngModule.factory('CentreCounts', CentreCountsFactory)
