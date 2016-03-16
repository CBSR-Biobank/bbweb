/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  //annotationMaxValueCount.$inject = [];

  /**
   *
   */
  function AnnotationMaxValueCount() {
    var ALL_VALUES = [
      NONE(),
      SELECT_SINGLE(),
      SELECT_MULTIPLE()
    ];

    var service = {
      NONE:            NONE,
      SELECT_SINGLE:   SELECT_SINGLE,
      SELECT_MULTIPLE: SELECT_MULTIPLE,
      values:          values
    };
    return service;

    //-------

    function NONE()            { return 0; }
    function SELECT_SINGLE()   { return 1; }
    function SELECT_MULTIPLE() { return 2; }

    function values()          { return ALL_VALUES; }

  }

  return AnnotationMaxValueCount;
});
