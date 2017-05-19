/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  HasCollectionSpecimenDescriptionsFactory.$inject = [ 'biobankApi', 'CollectionSpecimenDescription' ];

  /**
   * Maintains an array of specimen specs.
   *
   * This is a mixin.
   */
  function HasCollectionSpecimenDescriptionsFactory(biobankApi, CollectionSpecimenDescription) {

    function HasCollectionSpecimenDescriptions() {}

    HasCollectionSpecimenDescriptions.prototype.validSpecimenDescriptions = function (specimenDescriptions) {
      var result;

      if (_.isUndefined(specimenDescriptions) || (specimenDescriptions.length <= 0)) {
        // there are no specimen specs types, nothing to validate
        return true;
      }
      result = _.find(specimenDescriptions, function (annotType) {
        return !CollectionSpecimenDescription.isValid(annotType);
      });

      return _.isUndefined(result);
    };

    return HasCollectionSpecimenDescriptions;
  }

  return HasCollectionSpecimenDescriptionsFactory;
});
