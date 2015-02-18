define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.factory('SpecimenGroupDataSet', SpecimenGroupDataSetFactory);

  SpecimenGroupDataSetFactory.$inject = ['SpecimenGroupSet'];

  function SpecimenGroupDataSetFactory(SpecimenGroupSet) {

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
     * @param {SpecimenGroupSet} options.studySpecimenGroupSet all the collection event specimen groups for
     * the study.
     *
     * Only one of options.studySpecimenGroups or options.studySpecimenGroupSet should be used, but not both.
     */
    function SpecimenGroupDataSet(dataItems, options) {
      var self = this;

      self.dataItems = dataItems || [];

      options = options || {};

      if (options.studySpecimenGroups && options.studySpecimenGroupSet) {
        throw new Error('cannot create with both specimenGroups and specimenGroupSet');
      }

      if (options.studySpecimenGroups) {
        self.specimenGroupSet = new SpecimenGroupSet(options.studySpecimenGroups);
      }

      if (options.studySpecimenGroupSet) {
        self.specimenGroupSet = options.studySpecimenGroupSet;
      }

      if (self.specimenGroupSet) {
        _.each(self.dataItems, function (item) {
          item.specimenGroup = self.specimenGroupSet.get(item.specimenGroupId);
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
      var foundItem = _.findWhere(this.dataItems, {specimenGroupId: specimenGroupId});
      if (foundItem === undefined) {
        throw new Error('specimen group data with id not found: ' + specimenGroupId);
      }

      return foundItem;
    };

    /**
     * Allows adding multiple items with a empty ID (i.e. ''). If id is not empty then duplicate items
     * are not allowed.
     */
    SpecimenGroupDataSet.prototype.add = function (item) {
      if (!this.specimenGroupSet) {
        throw new Error('study has no specimen groups');
      }

      if (item.id && (item.id !== '')) {
        var foundItem = _.findWhere(this.dataItems, {specimenGroupId: item.id});
        if (foundItem !== undefined) {
          throw new Error('specimen group data already exists: ' + item.id);
        }
      }
      item = _.clone(item);
      if (item.specimenGroupId) {
        item.specimenGroup = this.specimenGroupSet.get(item.specimenGroupId);
      }
      this.dataItems.push(item);
    };

    /**
     * Removes a specimen group data item. Note that there can be multiple items with an empty ID.
     *
     * @param {string} atDataItemId the ID of the specimen group to remove.
     */
    SpecimenGroupDataSet.prototype.remove = function (atDataItemId) {
      var foundItem = _.findWhere(this.dataItems, {specimenGroupId: atDataItemId});
      if (foundItem === undefined) {
        throw new Error('specimen group data with id not found: ' + atDataItemId);
      }

      this.dataItems = _.without(this.dataItems, foundItem);
    };

    SpecimenGroupDataSet.prototype.getAsString = function () {
      if (this.dataItems.length === 0) {
        return '';
      }
      return _.map(this.dataItems, function (item) {
        return item.specimenGroup.name + ' (' + item.maxCount + ', ' + item.amount +
          ' ' + item.specimenGroup.units + ')';
      }).join(', ');
    };

    return SpecimenGroupDataSet;
  }

});
