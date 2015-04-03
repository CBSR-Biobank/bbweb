/* global define */
define(['underscore'], function(_) {
  'use strict';

  CollectionEventTypeFactory.$inject = [
    'funutils',
    'validationService',
    'biobankApi',
    'ConcurrencySafeEntity',
    'SpecimenGroupDataSet',
    'AnnotationTypeDataSet'
  ];

  /**
   * Factory for collectionEventTypes.
   */
  function CollectionEventTypeFactory(funutils,
                                      validationService,
                                      biobankApi,
                                      ConcurrencySafeEntity,
                                      SpecimenGroupDataSet,
                                      AnnotationTypeDataSet) {
    var requiredKeys = [
      'id',
      'studyId',
      'version',
      'name',
      'recurring',
      'specimenGroupData',
      'annotationTypeData'
    ];

    var validateIsMap = validationService.condition1(
      validationService.validator('must be a map', _.isObject));

    var createObj = funutils.partial1(validateIsMap, _.identity);

    var validateObj = funutils.partial(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys.apply(null, requiredKeys))),
      createObj);

    var validateAnnotationTypeData = funutils.partial(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys('annotationTypeId', 'required'))),
      createObj);

    var validateSpecimenGroupData = funutils.partial(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys('specimenGroupId',
                                                              'maxCount',
                                                              'amount'))),
      createObj);

    /**
     * Creates a collection event type object with helper methods.
     *
     * @param {Object} collectionEventType the collection event type returned by the server.
     *
     * @param {Study} options.study the study this collection even type belongs to.
     *
     * @param {SpecimenGroup array} options.studySpecimenGroups all specimen groups for the study. Should be a
     * list returned by the server.
     *
     * @param {AnnotationType array} options.studyAnnotationTypes all the collection event annotation types
     * for the study. Should be a list returned by the server.
     *
     * @param {Array} options.studySpecimenGroups all specimen groups for the study.
     *
     * @param {Array} options.studyAnnotationTypes all the collection event annotation types for the
     * study.
     */
    function CollectionEventType(collectionEventType, options) {
      var self = this;

      collectionEventType = collectionEventType || {};
      ConcurrencySafeEntity.call(self, collectionEventType);

      _.extend(self, _.defaults(collectionEventType, {
        studyId:            null,
        name:               '',
        description:        null,
        recurring:          false,
        specimenGroupData:  [],
        annotationTypeData: []
      }));

      options = options || {};

      if (options.studySpecimenGroups) {
        self.studySpecimenGroups(options.studySpecimenGroups);
      }

      if (options.studyAnnotationTypes) {
        self.studyAnnotationTypes(options.studyAnnotationTypes);
      }
    }

    CollectionEventType.prototype = Object.create(ConcurrencySafeEntity.prototype);

    /**
     * Used by promise code, so it must return an error rather than throw one.
     */
    CollectionEventType.create = function (obj) {
      var validation = validateObj(obj), atdValid, sgdValid;

      if (!_.isObject(validation)) {
        return new Error('invalid object: ' + validation);
      }

      atdValid = _.reduce(obj.annotationTypeData, function(memo, atd) {
        var validation = validateAnnotationTypeData(atd);
        return memo && _.isObject(validation);
      }, true);

      if (!atdValid) {
        return new Error('invalid object from server: bad annotation type data');
      }

      sgdValid = _.reduce(obj.specimenGroupData, function(memo, sgd) {
        var validation = validateSpecimenGroupData(sgd);
        return memo && _.isObject(validation);
      }, true);

      if (!sgdValid) {
        return new Error('invalid object from server: bad specimen group data');
      }

      return new CollectionEventType(obj);
    };

    CollectionEventType.get = function(studyId, id) {
      return biobankApi.get(uri(studyId) + '?cetId=' + id)
        .then(function(reply) {
          return CollectionEventType.create(reply);
        });
    };

    CollectionEventType.list = function(studyId) {
      return biobankApi.get(uri(studyId)).then(function(reply) {
        return _.map(reply, function (cet) {
          return CollectionEventType.create(cet);
        });
      });
    };

    CollectionEventType.prototype.studySpecimenGroups = function (specimenGroups) {
      this.specimenGroupDataSet =
        new SpecimenGroupDataSet(this.specimenGroupData, {
          studySpecimenGroups: specimenGroups
        });
    };

    CollectionEventType.prototype.studyAnnotationTypes = function (annotationTypes) {
      this.annotationTypeDataSet =
        new AnnotationTypeDataSet(this.annotationTypeData, {
          studyAnnotationTypes: annotationTypes
        });
    };

    CollectionEventType.prototype.addOrUpdate = function (annotationTypes) {
      var self = this,
          cmd = _.extend(_.pick(self,
                                'studyId',
                                'name',
                                'recurring'),
                         funutils.pickOptional(self, 'description'));

      if (self.specimenGroupDataSet && (self.specimenGroupDataSet.dataItems.length > 0)) {
        cmd.specimenGroupData = self.specimenGroupDataSet.getSpecimenGroupData();
      } else {
        cmd.specimenGroupData = self.specimenGroupData;
      }

      if (self.annotationTypeDataSet && (self.annotationTypeDataSet.dataItems.length > 0)) {
        cmd.annotationTypeData = self.annotationTypeDataSet.getAnnotationTypeData();
      } else {
        cmd.annotationTypeData = self.annotationTypeData;
      }

      return addOrUpdateInternal().then(function(reply) {
        return CollectionEventType.create(reply);
      });

      // --

      function addOrUpdateInternal() {
        if (self.isNew()) {
          return biobankApi.post(uri(self.studyId), cmd);
        }
        _.extend(cmd, { id: self.id, expectedVersion: self.version });
        return biobankApi.put(uri(self.studyId, self.id), cmd);
      }
    };

    CollectionEventType.prototype.remove = function () {
      return biobankApi.del(uri(this.studyId, this.id, this.version));
    };

    CollectionEventType.prototype.specimenGroupDataSize = function () {
      return this.specimenGroupDataSet.size();
    };

    CollectionEventType.prototype.allSpecimenGroupDataIds = function () {
      return this.specimenGroupDataSet.allIds();
    };

    CollectionEventType.prototype.getSpecimenGroupData = function (specimenGroupId) {
      if (this.specimenGroupDataSet) {
        return this.specimenGroupDataSet.get(specimenGroupId);
      }
      throw new Error('no data items');
    };

    CollectionEventType.prototype.getSpecimenGroupsAsString = function () {
      if (this.specimenGroupDataSet) {
        return this.specimenGroupDataSet.getAsString();
      }
      throw new Error('no data items');
    };

    CollectionEventType.prototype.annotationTypeDataSize = function () {
      return this.annotationTypeDataSet.size();
    };

    CollectionEventType.prototype.allAnnotationTypeDataIds = function () {
      return this.annotationTypeDataSet.allIds();
    };

    CollectionEventType.prototype.getAnnotationTypeData = function (annotationTypeId) {
      if (this.annotationTypeDataSet) {
        return this.annotationTypeDataSet.get(annotationTypeId);
      }
      throw new Error('no data items');
    };

    CollectionEventType.prototype.getAnnotationTypesAsString = function () {
      if (this.annotationTypeDataSet) {
        return this.annotationTypeDataSet.getAsString();
      }
      throw new Error('no data items');
    };

    function uri(studyId, ceventTypeId, version) {
      var result = '/studies';
      if (arguments.length <= 0) {
        throw new Error('study id not specified');
      } else {
        result += '/' + studyId + '/cetypes';

        if (arguments.length > 1) {
          result += '/' + ceventTypeId;
        }

        if (arguments.length > 2) {
          result += '/' + version;
        }
      }
      return result;
    }

    /** return constructor function */
    return CollectionEventType;
  }

  return CollectionEventTypeFactory;
});
