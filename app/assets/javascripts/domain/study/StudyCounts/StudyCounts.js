/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 *
 */
/* @ngInject */
function StudyCountsFactory(biobankApi) {

  function StudyCounts(options) {
    var defaults = {
      total:    0,
      disabled: 0,
      enabled:  0,
      retired:  0
    };

    options = options || {};
    _.extend(this, defaults, _.pick(options, _.keys(defaults)));
  }

  StudyCounts.get = function () {
    return biobankApi.get('/api/studies/counts').then(function (response) {
      return new StudyCounts({
        total:    response.total,
        disabled: response.disabledCount,
        enabled:  response.enabledCount,
        retired:  response.retiredCount
      });
    });
  };

  return StudyCounts;
}

export default ngModule => ngModule.factory('StudyCounts', StudyCountsFactory)
