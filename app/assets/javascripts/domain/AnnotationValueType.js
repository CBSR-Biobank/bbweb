/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * The types allowed for an {@link domain.annotations.AnnotationType AnnotationType}'s valueType.
   *
   * @enum {string}
   * @memberOf domain
   */
  var AnnotationValueType = {
    /** The annotation contains a text value. */
    TEXT:      'Text',

    /** The annotation contains a number value (whole, floating point, etc). */
    NUMBER:    'Number',

    /** The annotation contains a date and time value. */
    DATE_TIME: 'DateTime',

    /** The annotation contains a predefined set of value(s) to select from. */
    SELECT:    'Select'
  };

  return AnnotationValueType;
});
