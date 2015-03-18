define(['underscore'], function(_) {
  'use strict';

  //SpecimenGroupSetFactory.$inject = [];

  /**
   * A set of specimen groups.
   */
  function SpecimenGroupSetFactory() {

    /**
     * @param specimenGroups a list of sepcimen groups returned by the server.
     */
    function SpecimenGroupSet(specimenGroups) {
      if (_.isUndefined(specimenGroups)) {
        throw new Error('specimenGroups is undefined');
      }
      this.specimenGroups = _.indexBy(specimenGroups, 'id');
    }

    /**
     * Returns the specimen group with the given id.
     *
     * @param id the ID of the required wspecimen group.
     */
    SpecimenGroupSet.prototype.get = function (id) {
      var result = this.specimenGroups[id];
      if (!result) {
        throw new Error('specimen group not found: ' + id);
      }
      return result;
    };

    /**
     * Returns the specimen group with the given id or undefined if it does not exist.
     *
     * @param id the ID of the required wspecimen group.
     */
    SpecimenGroupSet.prototype.find = function (id) {
      return this.specimenGroups[id];
    };

    return SpecimenGroupSet;
  }

  return SpecimenGroupSetFactory;
});
