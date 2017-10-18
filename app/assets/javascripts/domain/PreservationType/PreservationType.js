/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 * A standardised set of methods for preserving and storing {@link Specimen}s.
 * Potential examples include: frozen specimen, RNA later, fresh specimen,
 * slide, etc.
 *
 * @enum {string}
 * @memberOf domain
 */
const PreservationType = {
  FROZEN_SPECIMEN: 'Frozen Specimen',
  RNA_LATER:       'RNA Later',
  FRESH_SPECIMEN:  'Fresh Specimen',
  SLIDE:           'Slide'
};


export default ngModule => ngModule.constant('PreservationType', PreservationType)
