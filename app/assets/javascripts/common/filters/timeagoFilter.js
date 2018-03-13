/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import moment from 'moment';

/**
 * Originally taken from link below and then modified.
 *
 * @return {AngularJSFilter} the factory function for this filter.
 */
/* @ngInject */
function timeagoFilterFactory(gettextCatalog) {

  /**
   * An AngualrJS Filter.
   *
   * @memberOf common.filters
   */
  class TimeagoFilter {

    /**
     * Displays the relative time for a Date.
     *
     * @param {Date} time a time in the past.
     *
     * @return {string} the amount of time from now to the time in the past.
     *
     * @example
     * // returns "4 years ago"
     * filter(new Date('January 1, 2014'));
     */
    static filter(time) {
      if(!time) {
        return gettextCatalog.getString('never');
      }
      return moment(time).fromNow();
    }

  }

  return TimeagoFilter.filter;
}

export default ngModule => ngModule.filter('timeago', timeagoFilterFactory)
