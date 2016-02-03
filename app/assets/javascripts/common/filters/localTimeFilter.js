/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  localTimeFilterFactory.$inject = ['timeService'];

  function localTimeFilterFactory(timeService) {
    return localTimeFilter;

    /**
     *
     */
    function localTimeFilter(time) {
      return timeService.timeToDisplayString(time);
    }

  }

  return localTimeFilterFactory;

});
