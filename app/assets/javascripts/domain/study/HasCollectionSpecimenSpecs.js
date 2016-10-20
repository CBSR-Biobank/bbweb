/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  HasCollectionSpecimenSpecsFactory.$inject = [ 'biobankApi', 'CollectionSpecimenSpec' ];

  /**
   * Maintains an array of specimen specs.
   *
   * This is a mixin.
   */
  function HasCollectionSpecimenSpecsFactory(biobankApi, CollectionSpecimenSpec) {

    function HasCollectionSpecimenSpecs() {}

    HasCollectionSpecimenSpecs.prototype.validSpecimenSpecs = function (specimenSpecs) {
      var result;

      if (_.isUndefined(specimenSpecs) || (specimenSpecs.length <= 0)) {
        // there are no specimen specs types, nothing to validate
        return true;
      }
      result = _.find(specimenSpecs, function (annotType) {
        return !CollectionSpecimenSpec.isValid(annotType);
      });

      return _.isUndefined(result);
    };

    return HasCollectionSpecimenSpecs;
  }

  return HasCollectionSpecimenSpecsFactory;
});
