/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  //PreservationTemperatureType.$inject = [];

  /**
   *
   */
  function PreservationTemperatureType() {
    var ALL_VALUES = [
      PLUS_4_CELCIUS(),
      MINUS_20_CELCIUS(),
      MINUS_80_CELCIUS(),
      MINUS_180_CELCIUS(),
      ROOM_TEMPERATURE()
    ];

    var service = {
      PLUS_4_CELCIUS:    PLUS_4_CELCIUS,
      MINUS_20_CELCIUS:  MINUS_20_CELCIUS,
      MINUS_80_CELCIUS:  MINUS_80_CELCIUS,
      MINUS_180_CELCIUS: MINUS_180_CELCIUS,
      ROOM_TEMPERATURE:  ROOM_TEMPERATURE,
      values:            values
    };
    return service;

    //-------

    function PLUS_4_CELCIUS()    { return '4 C'; }
    function MINUS_20_CELCIUS()  { return '-20 C'; }
    function MINUS_80_CELCIUS()  { return '-80 C'; }
    function MINUS_180_CELCIUS() { return '-180 C'; }
    function ROOM_TEMPERATURE()  { return 'Room Temperature'; }

    function values()            { return ALL_VALUES; }

  }

  return PreservationTemperatureType;
});
