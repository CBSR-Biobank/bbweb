/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 * Maintains an array of specimen specs.
 *
 * This is a mixin.
 */
/* @ngInject */
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

export default ngModule => ngModule.factory('HasCollectionSpecimenDescriptions',
                                           HasCollectionSpecimenDescriptionsFactory)
