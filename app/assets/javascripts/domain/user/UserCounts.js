define([], function() {
  'use strict';

  UserCountsFactory.$inject = ['biobankApi'];

  /**
   *
   */
  function UserCountsFactory(biobankApi) {

    function UserCounts(options) {
      options = options || {};

      this.total      = options.total || 0;
      this.registered = options.registered || 0;
      this.active     = options.active || 0;
      this.locked     = options.locked || 0;
    }

    UserCounts.get = function () {
      return biobankApi.get('/users/counts').then(function (response) {
        return new UserCounts({
          total:      response.total,
          registered: response.registeredCount,
          active:     response.activeCount,
          locked:     response.lockedCount
        });
      });
    };

    return UserCounts;
  }

  return UserCountsFactory;
});
