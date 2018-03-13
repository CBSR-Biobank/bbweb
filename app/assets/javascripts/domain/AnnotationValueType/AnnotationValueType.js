/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * The types allowed for an {@link domain.AnnotationType AnnotationType}'s valueType.
 *
 * @enum {string}
 * @memberOf domain
 */
const AnnotationValueType = {
  /**
   * The annotation contains a text value.
   *
   * @type {string}
   */
  TEXT:      'text',

  /**
   * The annotation contains a number value (whole, floating point, etc).
   *
   * @type {string}
   */
  NUMBER:    'number',

  /**
   * The annotation contains a date and time value.
   *
   * @type {string}
   */
  DATE_TIME: 'datetime',

  /**
   * The annotation contains a predefined set of value(s) to select from.
   *
   * @type {string}
   */
  SELECT:    'select'
};

export default ngModule => ngModule.constant('AnnotationValueType', AnnotationValueType)
