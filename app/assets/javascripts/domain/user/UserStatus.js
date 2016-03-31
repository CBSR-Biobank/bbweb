/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
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

      values:    values,
      label:    label
    };
    return service;

    //-------

    function REGISTERED() { return 'RegisteredUser'; }
    function ACTIVE()     { return 'ActiveUser'; }
    function LOCKED()     { return 'LockedUser'; }

    function values()    { return ALL_VALUES; }

    function label(status) {
      switch (status) {
      case REGISTERED():
        return 'Registered';
      case ACTIVE():
        return 'Active';
      case LOCKED():
        return 'Locked';
      }
      throw new Error('invalid status for user: ' + status);
    }
  }

  return UserStatus;
});
