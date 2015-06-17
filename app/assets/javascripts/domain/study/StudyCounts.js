define(['underscore'], function(_) {
  'use strict';

  StudyCountsFactory.$inject = ['biobankApi'];

  /**
   *
   */
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
      return biobankApi.get('/studies/counts').then(function (response) {
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

  return StudyCountsFactory;
});
