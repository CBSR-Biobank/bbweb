/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/* @ngInject */
function CentreCountsFactory(biobankApi) {

  /**
   * Contains the counts of {@link domain.centres.Centre Centres} in different states.
   *
   * @memberOf domain.centres
   */
  class CentreCounts {

    /**
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     */
    constructor(options = {}) {
      /**
       * The count of all centres.
       *
       * @name domain.centres.CentreCounts#total
       * @type {int}
       */

      /**
       * The count of centres in {@link domain.centres.centreState.CentreState.ENABLED ENABLED} state.
       *
       * @name domain.centres.CentreCounts#enabled
       * @type {int}
       */

      /**
       * The count of centres in {@link domain.centres.centreState.CentreState.DISABLED DISABLED} state.
       *
       * @name domain.centres.CentreCounts#disabled
       * @type {int}
       */

      const defaults = {
        total:    0,
        disabled: 0,
        enabled:  0
      };

      Object.assign(this,
                    defaults,
                    _.pick(options, Object.keys(defaults)));
    }

    /**
     * Requests the {@link domain.centres.Centre Centre} counts from the server.
     *
     * @return {Promise<domain.centres.CentreCounts>}
     */
    static get() {
      return biobankApi.get('/api/centres/counts')
        .then(response => new CentreCounts({
          total:    response.total,
          disabled: response.disabledCount,
          enabled:  response.enabledCount
        }))
    }
  }

  return CentreCounts;
}

export default ngModule => ngModule.factory('CentreCounts', CentreCountsFactory)
