define(['../module', 'moment'], function(module, moment) {
  'use strict';

  module.filter('timeago', timeagoFactory);

  //timeagoFactory.$inject = [];

  /**
   * Originally taken from link below and then modified.
   *
   * http://stackoverflow.com/questions/14774486/use-jquery-timeago-or-momentjs-and-angularjs-together
   */
  function timeagoFactory() {
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

});
