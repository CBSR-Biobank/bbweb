/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/* @ngInject */
function StudyCountsFactory(biobankApi) {

  /**
   * Contains the counts of {@link domain.studies.Study Studies} in different states.
   *
   * @memberOf domain.studies
   */
  class StudyCounts {

    constructor(options = {}) {
      /**
       * The count of all studies.
       *
       * @name domain.studies.StudyCounts#total
       * @type {int}
       */

      /**
       * The count of studies in {@link domain.studies.StudyState.StudyState.ENABLED ENABLED} state.
       *
       * @name domain.studies.StudyCounts#enabled
       * @type {int}
       */

      /**
       * The count of studies in {@link domain.studies.StudyState.StudyState.DISABLED DISABLED} state.
       *
       * @name domain.studies.StudyCounts#disabled
       * @type {int}
       */

      /**
       * The count of studies in {@link domain.studies.StudyState.StudyState.RETIRED RETIRED} state.
       *
       * @name domain.studies.StudyCounts#retired
       * @type {int}
       */

      const defaults = {
        total:    0,
        disabled: 0,
        enabled:  0,
        retired:  0
      };

      Object.assign(this,
                    defaults,
                    _.pick(options, Object.keys(defaults)));
    }

    /**
     * Requests the {@link domain.studies.Study Study} counts from the server.
     *
     * @return {Promise<domain.studies.StudyCounts>}
     */
    static get() {
      return biobankApi.get('/api/studies/counts')
        .then((response) => new StudyCounts({
          total:    response.total,
          disabled: response.disabledCount,
          enabled:  response.enabledCount,
          retired:  response.retiredCount
        }));
  }
  }

  return StudyCounts;
}

export default ngModule => ngModule.factory('StudyCounts', StudyCountsFactory)
