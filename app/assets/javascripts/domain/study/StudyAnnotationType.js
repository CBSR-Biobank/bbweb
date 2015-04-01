define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  StudyAnnotationTypeFactory.$inject = [
    'funutils',
    'biobankApi',
    'AnnotationValueType',
    'AnnotationMaxValueCount',
    'validationService',
    'studyAnnotationTypeValidation',
    'AnnotationType'
  ];

  /**
   *
   */
  function StudyAnnotationTypeFactory(funutils,
                                      biobankApi,
                                      AnnotationValueType,
                                      AnnotationMaxValueCount,
                                      validationService,
                                      studyAnnotationTypeValidation,
                                      AnnotationType) {

    function StudyAnnotationType(obj) {
      obj = obj || {};
      AnnotationType.call(this, obj);
      _.extend(this, _.defaults(obj, { studyId: null }));
      this._service = null;
    }

    StudyAnnotationType.prototype = Object.create(AnnotationType.prototype);

    /**
     * Factory function to get all study annotation types for studyId.
     *
     * @param {String} studyId - the ID for the parent study.
     */
    StudyAnnotationType.list = function (validator, createFn, annotationTypeUriPart, studyId) {
      return biobankApi.get('/studies/' + studyId + '/' + annotationTypeUriPart).then(function (reply) {
        return _.map(reply, function(obj) {
          return create(validator, createFn, obj);
        });
      });
    };

    /**
     * Factory function to get one study annotation type.
     *
     * @param {String} studyId - the ID for the parent study.
     *
     * @param {String} annotationTypeId - the ID of the annotaiton type.
     */
    StudyAnnotationType.get = function (validator,
                                        createFn,
                                        annotationTypeUriPart,
                                        studyId,
                                        annotationTypeId) {
      return biobankApi.get(
        '/studies/' + studyId + '/' + annotationTypeUriPart + '?annotationTypeId=' + annotationTypeId
      ).then(function (obj) {
        return create(validator, createFn, obj);
      });
    };

    /**
     * Sends a command, to the server, to add or update to a study annotation type.
     */
    StudyAnnotationType.prototype.addOrUpdate = function (validator, createFn) {
      var self = this;
      if (self._service === null) {
        throw new Error('_service is null');
      }

      return self._service.addOrUpdate(self).then(function(reply) {
        return create(validator, createFn, reply);
      });
    };

    StudyAnnotationType.prototype.remove = function () {
      var self = this;
      if (self._service === null) {
        throw new Error('_service is null');
      }
      return self._service.remove(self);
    };

    /**
     * Called when the annotation type's value type has been changed.
     */
    StudyAnnotationType.prototype.valueTypeChanged = function () {
      if (!this.isValueTypeSelect()) {
        this.maxValueCount = null;
      }
      this.options = [];
    };

    function create(validator, createFn, obj) {
      var validation = validator(obj);

      if (!_.isObject(validation)) {
        return new Error('annotation type from server fails validation: ' + validation);
      }

      return createFn(obj);
    }

    return StudyAnnotationType;
  }

  return StudyAnnotationTypeFactory;
});
