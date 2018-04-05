/**
 * AngularJS Constants used for defining specimen types.
 *
 * @namespace domain.PreservationType
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * A standardised set of methods for preserving and storing {@link domain.participants.Specimen Specimens}.
 *
 * Potential examples include: frozen specimen, RNA later, fresh specimen, slide, etc.
 *
 * @enum {string}
 * @memberOf domain.PreservationType
 */
const PreservationType = {
  FROZEN_SPECIMEN: 'Frozen Specimen',
  RNA_LATER:       'RNA Later',
  FRESH_SPECIMEN:  'Fresh Specimen',
  SLIDE:           'Slide'
};


export default ngModule => ngModule.constant('PreservationType', PreservationType)
