define(['../module'], function(module) {
  'use strict';

  module.factory('CentreCounts', CentreCountsFactory);

  CentreCountsFactory.$inject = ['biobankApi'];

  /**
   *
   */
  function CentreCountsFactory(biobankApi) {

    function CentreCounts(options) {
      options = options || {};

      this.total    = options.total || 0;
      this.disabled = options.disabled || 0;
      this.enabled  = options.enabled || 0;
    }

    CentreCounts.get = function () {
      return biobankApi.call('GET', '/centres/counts').then(function (response) {
        return new CentreCounts({
          total:    response.total,
          disabled: response.disabledCount,
          enabled:  response.enabledCount
        });
      });
    };

    return CentreCounts;
  }

});
