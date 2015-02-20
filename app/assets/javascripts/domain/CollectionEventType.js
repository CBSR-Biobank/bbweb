/* global define */
define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.factory('CollectionEventType', CollectionEventTypeFactory);

  CollectionEventTypeFactory.$inject = [
    'SpecimenGroupSet',
    'AnnotationTypeSet',
    'SpecimenGroupDataSet',
    'AnnotationTypeDataSet'
  ];

  /**
   * Factory for collectionEventTypes.
   */
  function CollectionEventTypeFactory(SpecimenGroupSet,
                                      AnnotationTypeSet,
                                      SpecimenGroupDataSet,
                                      AnnotationTypeDataSet) {


    /**
     * Creates a collection event type object with helper methods.
     *
     * @param {Study} study the study this collection even type belongs to.
     *
     * @param {CollectionEventType} collectionEventType the collection event type returned by the server.
     *
     * @param {SpecimenGroup array} options.studySpecimenGroups all specimen groups for the study. Should be a
     * list returned by the server.
     *
     * @param {AnnotationType array} options.studyAnnotationTypes all the collection event annotation types
     * for the study. Should be a list returned by the server.
     *
     * @param {SpecimenGroupSet} options.studySpecimenGroupSet all specimen groups for the study.
     *
     * @param {AnnotationTypeSet} options.studyAnnotationTypeSet all the collection event annotation types for the
     * study.
     */
    function CollectionEventType(study, collectionEventType, options) {
      var self = this;

      _.extend(self, _.omit(collectionEventType, 'specimenGroupData', 'annotationTypeData'));

      self.isNew               = !self.id;
      self.study               = study;
      self.studyId             = study.id;
      self.specimenGroupData   = self.specimenGroupData || [];

      options = options || {};

      self.specimenGroupDataSet = new SpecimenGroupDataSet(
        collectionEventType.specimenGroupData,
        _.pick(options, 'studySpecimenGroups', 'studySpecimenGroupSet'));

      self.annotationTypeDataSet = new AnnotationTypeDataSet(
        collectionEventType.annotationTypeData,
        _.pick(options, 'studyAnnotationTypes', 'studyAnnotationTypeSet'));
    }

    CollectionEventType.prototype.specimenGroupDataSize = function () {
      return this.specimenGroupDataSet.size();
    };

    CollectionEventType.prototype.allSpecimenGroupDataIds = function () {
      return this.specimenGroupDataSet.allIds();
    };

    CollectionEventType.prototype.getSpecimenGroupData = function (specimenGroupId) {
      return this.specimenGroupDataSet.get(specimenGroupId);
    };

    CollectionEventType.prototype.getSpecimenGroupsAsString = function () {
      return this.specimenGroupDataSet.getAsString();
    };

    CollectionEventType.prototype.annotationTypeDataSize = function () {
      return this.annotationTypeDataSet.size();
    };

    CollectionEventType.prototype.allAnnotationTypeDataIds = function () {
      return this.annotationTypeDataSet.allIds();
    };

    CollectionEventType.prototype.getAnnotationTypeData = function (annotationTypeId) {
      return this.annotationTypeDataSet.get(annotationTypeId);
    };

    CollectionEventType.prototype.getAnnotationTypesAsString = function () {
      return this.annotationTypeDataSet.getAsString();
    };

    /**
     * Returns a collection event type as expected by the server.
     */
    CollectionEventType.prototype.getServerCeventType = function () {
      var serverCeventType = _.pick(this,
                                    'id',
                                    'studyId',
                                    'version',
                                    'timeAdded',
                                    'timeModified',
                                    'name',
                                    'description',
                                    'recurring');
      serverCeventType.specimenGroupData  = this.specimenGroupDataSet.getSpecimenGroupData();
      serverCeventType.annotationTypeData = this.annotationTypeDataSet.getAnnotationTypeData();
      return serverCeventType;
    };

    /** return constructor function */
    return CollectionEventType;
  }

});
