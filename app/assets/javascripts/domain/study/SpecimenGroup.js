define([], function() {
  'use strict';

  module.factory('SpecimenGroup', SpecimenGroupFactory);

  //SpecimenGroupFactory.$inject = [];

  /**
   *
   */
  function SpecimenGroupFactory() {

    function SpecimenGroup() {

    }

    /**
     * Utility function that fetches a specimen group from a list based on the id.
     */
    SpecimenGroup.getUnits = function (specimenGroups, id) {
      if (!id) { return 'Amount'; }

      var sg = _.findWhere(specimenGroups, { id: id });
      if (sg) {
        return sg.units;
      }
      throw new Error('specimen group not found: ' + id);
    };

    return SpecimenGroup;
  }

});
