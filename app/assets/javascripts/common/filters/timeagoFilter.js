/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['moment'], function(moment) {
  'use strict';

  timeagoFilterFactory.$inject = ['gettextCatalog'];

  /**
   * Originally taken from link below and then modified.
   *
   * http://stackoverflow.com/questions/14774486/use-jquery-timeago-or-momentjs-and-angularjs-together
   */
  function timeagoFilterFactory(gettextCatalog) {
    return timeago;

    /*
     * @param time a Date
     */
    function timeago(time) {
      if(!time) {
        return gettextCatalog.getString('never');
      }
      return moment(time).fromNow();
    }

  }

  return timeagoFilterFactory;
});
