define(['underscore'], function(_) {
  'use strict';

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

      self.dataItems = _.map(dataItems, function (item) { return _.clone(item); });

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
      var foundItem;

      if (this.dataItems.length === 0) {
        throw new Error('no data items');
      }

      foundItem = _.findWhere(this.dataItems, {annotationTypeId: annotationTypeId});
      if (foundItem === undefined) {
        throw new Error('annotation type data with id not found: ' + annotationTypeId);
      }

      return foundItem;
    };

    AnnotationTypeDataSet.prototype.getAnnotationTypeData = function () {
      return _.map(this.dataItems, function (item) {
        return {
          annotationTypeId: item.annotationTypeId,
          required:         item.required
        };
      });
    };


    AnnotationTypeDataSet.prototype.getAsString = function () {
      if (this.dataItems.length === 0) {
        throw new Error('no data items');
      }
      return _.map(this.dataItems, function (atItem) {
        return atItem.annotationType.name + (atItem.required ? ' (Req)' : ' (N/R)');
      }, this).join(', ');
    };

    return AnnotationTypeDataSet;
  }

  return AnnotationTypeDataSetFactory;
});
