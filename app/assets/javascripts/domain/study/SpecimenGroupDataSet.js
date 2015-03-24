define(['underscore'], function(_) {
  'use strict';

  //SpecimenGroupDataSetFactory.$inject = [];

  function SpecimenGroupDataSetFactory() {

    /**
     * Maintains a set of specimenGroupData items. Only one specimen group data item with a non empty ID is
     * allowed in the set. A blank specimen group ID is allowed for adding new ones that are being edited by
     * the user.
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
    function SpecimenGroupDataSet(dataItems, options) {
      var self = this, specimenGroups;

      self.dataItems = _.map(dataItems, function (item) { return _.clone(item); });

      options = options || {};

      if (options.studySpecimenGroups) {
        specimenGroups = _.indexBy(options.studySpecimenGroups, 'id');

        _.each(self.dataItems, function (item) {
          item.specimenGroup = specimenGroups[item.specimenGroupId];
        });
      }
    }

    SpecimenGroupDataSet.prototype.size = function () {
      return this.dataItems.length;
    };

    /**
     * Returns the IDs of all data items.
     */
    SpecimenGroupDataSet.prototype.allIds = function () {
      return _.pluck(this.dataItems, 'specimenGroupId');
    };

    /**
     * Returns the specimen group with the given ID.
     */
    SpecimenGroupDataSet.prototype.get = function (specimenGroupId) {
      var foundItem;

      if (this.dataItems.length === 0) {
        throw new Error('no data items');
      }

      foundItem = _.findWhere(this.dataItems, {specimenGroupId: specimenGroupId});
      if (foundItem === undefined) {
        throw new Error('specimen group data with id not found: ' + specimenGroupId);
      }

      return foundItem;
    };

    SpecimenGroupDataSet.prototype.getSpecimenGroupData = function () {
      return _.map(this.dataItems, function (item) {
        return {
          specimenGroupId: item.specimenGroupId,
          maxCount:        item.maxCount,
          amount:          item.amount
        };
      });
    };

    SpecimenGroupDataSet.prototype.getAsString = function () {
      if (this.dataItems.length === 0) {
        throw new Error('no data items');
      }
      return _.map(this.dataItems, function (item) {
        return item.specimenGroup.name + ' (' + item.maxCount + ', ' + item.amount +
          ' ' + item.specimenGroup.units + ')';
      }).join(', ');
    };

    return SpecimenGroupDataSet;
  }

  return SpecimenGroupDataSetFactory;
});
