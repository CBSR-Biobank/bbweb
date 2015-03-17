define(['../module'], function(module) {
  'use strict';

  module.service('StudyStatus', StudyStatus);

  //StudyStatus.$inject = [];

  /**
   *
   */
  function StudyStatus() {
    var ALL_VALUES = [
      DISABLED(),
      ENABLED(),
      RETIRED()
    ];

    var service = {
      DISABLED: DISABLED,
      ENABLED:  ENABLED,
      RETIRED:  RETIRED,

      values:    values
    };
    return service;

    //-------

    function DISABLED() { return 'Disabled'; }
    function ENABLED()  { return 'Enabled'; }
    function RETIRED()  { return 'Retired'; }

    function values()    { return ALL_VALUES; }
  }

});
