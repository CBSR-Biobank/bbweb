define(['../module'], function(module) {
  'use strict';

  module.service('CentreStatus', CentreStatus);

  //CentreStatus.$inject = [];

  /**
   *
   */
  function CentreStatus() {
    var ALL_VALUES = [
      REGISTERED(),
      ACTIVE(),
      LOCKED(),
    ];

    var service = {
      REGISTERED: REGISTERED,
      ACTIVE:     ACTIVE,
      LOCKED:     LOCKED,

      values:    values
    };
    return service;

    //-------

    function REGISTERED() { return 'Registered'; }
    function ACTIVE()     { return 'Active'; }
    function LOCKED()     { return 'Locked'; }

    function values()    { return ALL_VALUES; }
  }

});
