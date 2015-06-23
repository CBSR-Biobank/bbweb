/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  //SpecimenGroupDataFactory.$inject = [];

  function SpecimenGroupDataFactory() {

    /**
     * Maintains a set of specimenGroupData items.
     *
     * This is a mixin. Only one specimen group data item with a non empty ID is allowed in the set. A blank
     * specimen group ID is allowed for adding new ones that are being edited by the user.
     *
     * @param {SpecimenGroupData} dataItems specimen group data as returned from server. Either from a
     * collection event type or specimen link type.
     *
     * @param {SpecimenGroup array} options.studySpecimenGroups all the specimen groups for the study. Should
     * be a list returned by the server.
     *
     * @param {Array} options.studySpecimenGroups all the collection event specimen groups for
     * the study.
     */
    return {
      studySpecimenGroups:       studySpecimenGroups,
      specimenGroupDataIds:      specimenGroupDataIds,
      getSpecimenGroupDataById:  getSpecimenGroupDataById,
      getSpecimenGroupData:      getSpecimenGroupData,
      getSpecimenGroupsAsString: getSpecimenGroupsAsString
    };

    //--

    /**
     * Used to cross reference the study's specimen groups to the respective annotation type data.
     */
    function studySpecimenGroups(specimenGroups) {
      /*jshint validthis:true */
      var specimenGroupsById = _.indexBy(specimenGroups, 'id');

      this.specimenGroupData = _.map(this.specimenGroupData, function (item) {
        item.specimenGroup = specimenGroupsById[item.specimenGroupId];
        return item;
      });
    }

    /**
     * Returns the IDs of all data items.
     */
    function specimenGroupDataIds() {
      /*jshint validthis:true */
      return _.pluck(this.specimenGroupData, 'specimenGroupId');
    }

    /**
     * Returns the specimen group with the given ID.
     */
    function getSpecimenGroupDataById(specimenGroupId) {
      /*jshint validthis:true */
      var foundItem;

      if (this.specimenGroupData.length <= 0) {
        throw new Error('no data items');
      }

      foundItem = _.findWhere(this.specimenGroupData, {specimenGroupId: specimenGroupId});
      if (foundItem === undefined) {
        throw new Error('specimen group data with id not found: ' + specimenGroupId);
      }
      return foundItem;
    }

    function getSpecimenGroupData() {
      /*jshint validthis:true */
      return _.map(this.specimenGroupData, function (item) {
        return {
          specimenGroupId: item.specimenGroupId,
          maxCount:        item.maxCount,
          amount:          item.amount
        };
      });
    }

    function getSpecimenGroupsAsString() {
      /*jshint validthis:true */
      if (this.specimenGroupData.length === 0) {
        throw new Error('no data items');
      }
      return _.map(this.specimenGroupData, function (item) {
        return item.specimenGroup.name + ' (' + item.maxCount + ', ' + item.amount +
          ' ' + item.specimenGroup.units + ')';
      }).join(', ');
    }
  }

  return SpecimenGroupDataFactory;
});
