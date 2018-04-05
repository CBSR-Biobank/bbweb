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
   * Contains the counts of {@link domain.users.User Users} in different states.
   *
   * @memberOf domain.users
   */
  class UserCounts {

    /**
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     */
    constructor(options = {}) {
      /**
       * The count of all users.
       *
       * @name domain.users.UserCounts#total
       * @type {int}
       */

      /**
       * The count of users in {@link domain.users.UserState.UserState.REGISTERED REGISTERED} state.
       *
       * @name domain.users.UserCounts#registered
       * @type {int}
       */

      /**
       * The count of users in {@link domain.users.UserState.UserState.ACTIVE ACTIVE} state.
       *
       * @name domain.users.UserCounts#active
       * @type {int}
       */

      /**
       * The count of users in {@link domain.users.UserState.UserState.LOCKED LOCKED} state.
       *
       * @name domain.users.UserCounts#locked
       * @type {int}
       */

      const defaults = {
        total:      0,
        registered: 0,
        active:     0,
        locked:     0
      };

      Object.assign(this,
                    defaults,
                    _.pick(options, Object.keys(defaults)));
    }

    static get() {
      return biobankApi.get('/api/users/counts')
        .then((response) => new UserCounts({
          total:      response.total,
          registered: response.registeredCount,
          active:     response.activeCount,
          locked:     response.lockedCount
        }));
    }
  }

  return UserCounts;
}

export default ngModule => ngModule.factory('UserCounts', UserCountsFactory)
