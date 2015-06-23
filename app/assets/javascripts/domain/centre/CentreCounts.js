/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  CentreCountsFactory.$inject = ['biobankApi'];

  /**
   *
   */
  function CentreCountsFactory(biobankApi) {

    function CentreCounts(options) {
      var defaults = {
        total:    0,
        disabled: 0,
        enabled:  0
      };

      options = options || {};
      _.extend(this, defaults, _.pick(options, _.keys(defaults)));
    }

    CentreCounts.get = function () {
      return biobankApi.get('/centres/counts').then(function (response) {
        return new CentreCounts({
          total:    response.total,
          disabled: response.disabledCount,
          enabled:  response.enabledCount
        });
      });
    };

    return CentreCounts;
  }

  return CentreCountsFactory;
});
