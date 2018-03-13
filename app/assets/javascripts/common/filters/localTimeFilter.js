/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */


/* @ngInject */
function localTimeFilterFactory(timeService) {

  /**
   * An AngualrJS Filter.
   *
   * @memberOf common.filters
   */
  class LocalTimeFilter {

    /**
     * Displays a date object as a local time.
     *
     * @param {Date} time - the date and /or time to convert.
     *
     * @return {string}
     */
    static filter(time) {
      return timeService.dateToDisplayString(time);
    }

  }

  return LocalTimeFilter.filter;
}

export default ngModule => ngModule.filter('localTime', localTimeFilterFactory)
