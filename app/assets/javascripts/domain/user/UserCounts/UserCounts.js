/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 *
 */
/* @ngInject */
function UserCountsFactory(biobankApi) {


  /**
   * The counts of users indexed by state.
   *
   * @memberOf domain.users
   */
  function UserCounts(options = {}) {
    var defaults = {
      total:      0,
      registered: 0,
      active:     0,
      locked:     0
    };

    _.extend(this, defaults, _.pick(options, _.keys(defaults)));
  }

  UserCounts.get = function () {
    return biobankApi.get('/api/users/counts').then(function (response) {
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

export default ngModule => ngModule.factory('UserCounts', UserCountsFactory)
