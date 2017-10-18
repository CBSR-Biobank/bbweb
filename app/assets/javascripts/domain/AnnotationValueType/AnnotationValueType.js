/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 * The types allowed for an {@link domain.annotations.AnnotationType AnnotationType}'s valueType.
 *
 * @enum {string}
 * @memberOf domain
 */
const AnnotationValueType = {
  /** The annotation contains a text value. */
  TEXT:      'text',

  /** The annotation contains a number value (whole, floating point, etc). */
  NUMBER:    'number',

  /** The annotation contains a date and time value. */
  DATE_TIME: 'datetime',

  /** The annotation contains a predefined set of value(s) to select from. */
  SELECT:    'select'
};

export default ngModule => ngModule.constant('AnnotationValueType', AnnotationValueType)
