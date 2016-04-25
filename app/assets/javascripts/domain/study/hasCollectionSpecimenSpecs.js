/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  hasCollectionSpecimenSpecsFactory.$inject = [ 'biobankApi', 'CollectionSpecimenSpec' ];

  /**
   * Maintains an array of specimen specs.
   *
   * This is a mixin.
   */
  function hasCollectionSpecimenSpecsFactory(biobankApi, CollectionSpecimenSpec) {

    var mixins = {
      validSpecimenSpecs:    validSpecimenSpecs
    };

    return mixins;

    //--

    function validSpecimenSpecs(specimenSpecs) {
      var result;

      if (_.isUndefined(specimenSpecs) || (specimenSpecs.length <= 0)) {
        // there are no annotation types, nothing to validate
        return true;
      }
      result = _.find(specimenSpecs, function (annotType) {
        return !CollectionSpecimenSpec.isValid(annotType);
      });

      return _.isUndefined(result);
    }

  }

  return hasCollectionSpecimenSpecsFactory;
});
