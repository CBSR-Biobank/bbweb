/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  CollectionSpecimenSpecsFactory.$inject = [ 'biobankApi', 'CollectionSpecimenSpec' ];

  /**
   * Maintains an array of specimen specs.
   *
   * This is a mixin.
   */
  function CollectionSpecimenSpecsFactory(biobankApi, CollectionSpecimenSpec) {

    var mixins = {
      validSpecimenSpecs:    validSpecimenSpecs,
      canRemoveSpecimenSpec: canRemoveSpecimenSpec
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
        return !CollectionSpecimenSpec.valid(annotType);
      });

      return _.isUndefined(result);
    }

    function canRemoveSpecimenSpec(specimenSpec) {
       /* jshint validthis:true */
     var self = this,
          found = _.findWhere(self.specimenSpecs,  { uniqueId: specimenSpec.uniqueId });
      return !_.isUndefined(found);
    }


  }

  return CollectionSpecimenSpecsFactory;
});
