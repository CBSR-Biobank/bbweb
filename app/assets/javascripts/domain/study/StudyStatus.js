/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

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

      values:   values,
      label:    label
    };
    return service;

    //-------

    function DISABLED() { return 'DisabledStudy'; }
    function ENABLED()  { return 'EnabledStudy'; }
    function RETIRED()  { return 'RetiredStudy'; }

    function values()    { return ALL_VALUES; }

    function label(status) {
      switch (status) {
      case DISABLED():
        return 'Disabled';
      case ENABLED():
        return 'Enabled';
      case RETIRED():
        return 'Retired';
      }
      throw new Error('invalid status for study: ' + status);
    }
  }

  return StudyStatus;
});
