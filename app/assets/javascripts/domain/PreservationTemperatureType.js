define(['./module'], function(module) {
  'use strict';

  module.service('PreservationTemperatureType', preservationTemperatureType);

  //preservationTemperatureType.$inject = [];

  /**
   *
   */
  function preservationTemperatureType() {
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

});
