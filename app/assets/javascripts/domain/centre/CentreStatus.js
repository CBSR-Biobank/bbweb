/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  //CentreStatus.$inject = [];

  /**
   *
   */
  function CentreStatus() {
    var ALL_VALUES = [
      DISABLED(),
      ENABLED()
    ];

    var service = {
      DISABLED: DISABLED,
      ENABLED:  ENABLED,

      values:    values
    };
    return service;

    //-------

    function DISABLED() { return 'DisabledCentre'; }
    function ENABLED()  { return 'EnabledCentre'; }

    function values()    { return ALL_VALUES; }
  }

  return CentreStatus;
});
