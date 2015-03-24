define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  StudyAnnotationTypeFactory.$inject = [
    'funutils',
    'biobankApi',
    'AnnotationValueType',
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
    StudyAnnotationType.list = function (validator, createFn, annotTypeUriPart, studyId) {
      return biobankApi.get('/studies/' + studyId + '/' + annotTypeUriPart).then(function (reply) {
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
                                        annotTypeUriPart,
                                        studyId,
                                        annotationTypeId) {
      return biobankApi.get(
        '/studies/' + studyId + '/' + annotTypeUriPart + '?annotTypeId=' + annotationTypeId
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
        this.maxValueCount = undefined;
      }
      this.options = [];
    };

    /**
     * Used to add an option. Should only be called when the value type is 'Select'.
     */
    StudyAnnotationType.prototype.addOption = function () {
      if (!this.isValueTypeSelect()) {
        throw new Error('value type is not select: ' + this.valueType);
      }
      this.options.push('');
    };

    /**
     * Used to remove an option. Should only be called when the value type is 'Select'.
     */
    StudyAnnotationType.prototype.removeOption = function (option) {
      if (this.options.length <= 1) {
        throw new Error('options is empty, cannot remove any more options');
      }
      this.options = _.without(this.options, option);
    };

    /**
     * Returns true if the maxValueCount value is valid.
     */
    StudyAnnotationType.prototype.maxValueCountValid = function () {
      return ((this.maxValueCount >= 1) && (this.maxValueCount <= 2));
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
