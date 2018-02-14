/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function localTimeFilterFactory(timeService) {
  return localTimeFilter;

  /**
   * @class localTimeFilter
   * @memberOf ng.common.filters
   *
   * @description An Angular filter that displays a <code>Date</code> object as a local time.
   */
  function localTimeFilter(time) {
    return timeService.dateToDisplayString(time);
  }

}

export default ngModule => ngModule.filter('localTime', localTimeFilterFactory)
