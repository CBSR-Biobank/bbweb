define([], function() {
  'use strict';

  //annotationValueType.$inject = [];

  /**
   *
   */
  function AnnotationValueType() {
    var ALL_VALUES = [
      TEXT(),
      NUMBER(),
      DATE_TIME(),
      SELECT()
    ];

    var service = {
      TEXT:      TEXT,
      NUMBER:    NUMBER,
      DATE_TIME: DATE_TIME,
      SELECT:    SELECT,
      values:    values
    };
    return service;

    //-------

    function TEXT()      { return 'Text'; }
    function NUMBER()    { return 'Number'; }
    function DATE_TIME() { return 'DateTime'; }
    function SELECT()    { return 'Select'; }

    function values()    { return ALL_VALUES; }

  }

  return AnnotationValueType;
});
