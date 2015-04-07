define(['underscore'], function(_) {
  'use strict';

  //AnnotationTypeDataFactory.$inject = [];

  function AnnotationTypeDataFactory() {

    /**
     * Maintains a set of annotationTypeData items.
     *
     * This is a mixin. Can hold two types of annotation types used by the study: collection event type and
     * speclimen link type. Only one annotation type data item with a non empty ID is allowed in the set. A
     * blank annotation type ID is allowed for adding new ones that are being edited by the user.
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
    return {
      studyAnnotationTypes:          studyAnnotationTypes,
      annotationTypeDataIds:         annotationTypeDataIds,
      getAnnotationTypeDataById:     getAnnotationTypeDataById,
      getAnnotationTypeData:         getAnnotationTypeData,
      getAnnotationTypeDataAsString: getAnnotationTypeDataAsString
    };

    /**
     * Used to cross reference the study's annotation types to the respective annotation type data.
     */
    function studyAnnotationTypes(annotationTypes) {
      /*jshint validthis:true */

      var annotationTypesById = _.indexBy(annotationTypes, 'id');

      _.each(this.annotationTypeData, function (item) {
        item.annotationType = annotationTypesById[item.annotationTypeId];
      });
    }

    /**
     * Returns the IDs of all data items.
     */
    function annotationTypeDataIds() {
      /*jshint validthis:true */
      return _.pluck(this.annotationTypeData, 'annotationTypeId');
    }

    /**
     * Returns the annotation type with the give ID.
     */
    function getAnnotationTypeDataById(annotationTypeId) {
      /*jshint validthis:true */
      var foundItem;

      if (this.annotationTypeData.length === 0) {
        throw new Error('no data items');
      }

      foundItem = _.findWhere(this.annotationTypeData, {annotationTypeId: annotationTypeId});
      if (foundItem === undefined) {
        throw new Error('annotation type data with id not found: ' + annotationTypeId);
      }

      return foundItem;
    }

    function getAnnotationTypeData() {
      /*jshint validthis:true */
      return _.map(this.annotationTypeData, function (item) {
        return {
          annotationTypeId: item.annotationTypeId,
          required:         item.required
        };
      });
    }


    function getAnnotationTypeDataAsString() {
      /*jshint validthis:true */
      return _.map(this.annotationTypeData, function (atItem) {
        return atItem.annotationType.name + (atItem.required ? ' (Req)' : ' (N/R)');
      }, this).join(', ');
    }
  }

  return AnnotationTypeDataFactory;
});
