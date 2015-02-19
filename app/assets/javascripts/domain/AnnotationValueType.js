define(['./module'], function(module) {
  'use strict';

  module.service('AnnotationValueType', annotationValueType);

  //annotationValueType.$inject = [];

  /**
   *
   */
  function annotationValueType() {
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

    function values()    { return ALL_VALUES; };

  }

});
