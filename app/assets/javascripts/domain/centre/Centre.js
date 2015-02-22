define(['./module', 'angular'], function(module, angular) {
  'use strict';

  module.factory('Centre', CentreFactory);

  CentreFactory.$inject = ['Location'];

  /**  *
   */
  function CentreFactory(Location) {

    /**
     * Centre is a value object.
     */
    function Centre(obj) {
      obj =  obj || {};

      this.id = obj.id || null;
    }

    return Centre;
  }

});
