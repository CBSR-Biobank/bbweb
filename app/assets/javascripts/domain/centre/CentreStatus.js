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

      values:   values,
      label:    label
    };
    return service;

    //-------

    function DISABLED() { return 'DisabledCentre'; }
    function ENABLED()  { return 'EnabledCentre'; }

    function values()    { return ALL_VALUES; }

    function label(status) {
      switch (status) {
      case DISABLED():
        return 'Disabled';
      case ENABLED():
        return 'Enabled';
      }
      return new Error('invalid status for centre' + status);
    }
  }

  return CentreStatus;
});
