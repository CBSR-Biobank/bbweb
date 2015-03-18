define([], function() {
  'use strict';

  //PreservationType.$inject = [];

  /**
   *
   */
  function PreservationType() {
    var ALL_VALUES = [
      FROZEN_SPECIMEN(),
      RNA_LATER(),
      FRESH_SPECIMEN(),
      SLIDE(),
    ];

    var service = {
      FROZEN_SPECIMEN: FROZEN_SPECIMEN,
      RNA_LATER:       RNA_LATER,
      FRESH_SPECIMEN:  FRESH_SPECIMEN,
      SLIDE:           SLIDE,
      values:          values
    };
    return service;

    //-------

    function FROZEN_SPECIMEN() { return 'Frozen Specimen'; }
    function RNA_LATER()       { return 'RNA Later'; }
    function FRESH_SPECIMEN()  { return 'Fresh Specimen'; }
    function SLIDE() { return 'Slide'; }

    function values()          { return ALL_VALUES; }
  }

  return PreservationType;
});
