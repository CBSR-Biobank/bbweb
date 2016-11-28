/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * @enum {string}
   * @memberOf domain.centres
   */
  var SpecimenState = {
    USABLE:   'usable',
    UNUSABLE: 'unusable'
  };

  return SpecimenState;
});
