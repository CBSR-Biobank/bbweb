/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  localTimeFilterFactory.$inject = ['timeService'];

  function localTimeFilterFactory(timeService) {
    return localTimeFilter;

    /**
     * @class common.localTimeFilter
     * @memberof common
     *
     * @description An Angular filter that displays a <code>Date</code> object as a local time.
     */
    function localTimeFilter(time) {
      return timeService.dateToDisplayString(time);
    }

  }

  return localTimeFilterFactory;

});
