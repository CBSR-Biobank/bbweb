/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['moment'], function(moment) {
  'use strict';

  //timeagoFactory.$inject = [];

  /**
   * Originally taken from link below and then modified.
   *
   * http://stackoverflow.com/questions/14774486/use-jquery-timeago-or-momentjs-and-angularjs-together
   */
  function timeagoFilterFactory() {
    return timeago;

    /*
     * @param time a Date
     */
    function timeago(time) {
      if(!time) {
        return 'never';
      }
      return moment(time).fromNow();
    }

  }

  return timeagoFilterFactory;
});
