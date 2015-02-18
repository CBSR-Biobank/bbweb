define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.factory('AnnotationTypeDataSet', AnnotationTypeDataSetFactory);

  AnnotationTypeDataSetFactory.$inject = ['AnnotationTypeSet'];

  function AnnotationTypeDataSetFactory(AnnotationTypeSet) {

    /**
     * Maintains a set of annotationTypeData items. Can hold two types on annotation types used by the study:
     * collection event type and speclimen link type. Only one annotation type data item with a non empty ID
     * is allowed in the set. A blank annotation type ID is allowed for adding new ones that are being edited
     * by the user.
     *
     * @param {AnnotationTypeData} dataItems annotation type data as returned from server. Either from a
     * collection event type or specimen link type.
     *
     * @param {AnnotationType array} options.studyAnnotationTypes all the annotation types
     * for the study. Should be a list returned by the server.
     *
     * @param {AnnotationTypeSet} options.studyAnnotationTypeSet all the collection event annotation types for
     * the study.
     *
     * Only one of options.studyAnnotationTypes or options.studyAnnotationTypeSet should be used, but not both.
     */
    function AnnotationTypeDataSet(dataItems, options) {
      var self = this;

      self.dataItems = dataItems || [];

      options = options || {};

      if (options.studyAnnotationTypes && options.studyAnnotationTypeSet) {
        throw new Error('cannot create with both annotationTypes and annotationTypeSet');
      }

      if (options.studyAnnotationTypes) {
        self.annotationTypeSet = new AnnotationTypeSet(options.studyAnnotationTypes);
      }

      if (options.studyAnnotationTypeSet) {
        self.annotationTypeSet = options.studyAnnotationTypeSet;
      }

      if (self.annotationTypeSet) {
        _.each(self.dataItems, function (item) {
          item.annotationType = self.annotationTypeSet.get(item.annotationTypeId);
        });
      }
    }

    AnnotationTypeDataSet.prototype.size = function () {
      return this.dataItems.length;
    };

    /**
     * Returns the IDs of all data items.
     */
    AnnotationTypeDataSet.prototype.allIds = function () {
      return _.pluck(this.dataItems, 'annotationTypeId');
    };

    /**
     * Returns the annotation type with the give ID.
     */
    AnnotationTypeDataSet.prototype.get = function (annotationTypeId) {
      var foundItem = _.findWhere(this.dataItems, {annotationTypeId: annotationTypeId});
      if (foundItem === undefined) {
        throw new Error('annotation type data with id not found: ' + annotationTypeId);
      }

      return foundItem;
    };

    /**
     * Allows adding multiple items with a empty ID (i.e. ''). If id is not empty then duplicate items
     * are not allowed.
     */
    AnnotationTypeDataSet.prototype.add = function (item) {
      if (item.id && (item.id !== '')) {
        var foundItem = _.findWhere(this.dataItems, {annotationTypeId: item.id});
        if (foundItem !== undefined) {
          throw new Error('annotation type data already exists: ' + item.id);
        }
      }
      item = _.clone(item);
      if (item.annotationTypeId) {
        item.annotationType = this.annotationTypeSet.get(item.annotationTypeId);
      }
      this.dataItems.push(item);
    };

    /**
     * Removes a annotation type data item. Note that there can be multiple items with an empty ID.
     *
     * @param {string} atDataItemId the ID of the annotation type to remove.
     */
    AnnotationTypeDataSet.prototype.remove = function (atDataItemId) {
      var foundItem = _.findWhere(this.dataItems, {annotationTypeId: atDataItemId});
      if (foundItem === undefined) {
        throw new Error('annotation type data with id not found: ' + atDataItemId);
      }

      this.dataItems = _.without(this.dataItems, foundItem);
    };

    AnnotationTypeDataSet.prototype.getAsString = function () {
      if (this.dataItems.length === 0) {
        return '';
      }
      return _.map(this.dataItems, function (atItem) {
        return atItem.annotationType.name + (atItem.required ? ' (Req)' : ' (N/R)');
      }, this).join(', ');
    };

    return AnnotationTypeDataSet;
  }

});
