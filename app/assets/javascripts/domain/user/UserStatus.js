define([], function() {
  'use strict';

  //UserStatus.$inject = [];

  /**
   *
   */
  function UserStatus() {
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

  return UserStatus;
});
