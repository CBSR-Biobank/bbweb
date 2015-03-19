define([], function() {
  'use strict';

  StudyCountsFactory.$inject = ['biobankApi'];

  /**
   *
   */
  function StudyCountsFactory(biobankApi) {

    function StudyCounts(options) {
      options = options || {};

      this.total    = options.total || 0;
      this.disabled = options.disabled || 0;
      this.enabled  = options.enabled || 0;
      this.retired  = options.retired || 0;
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
