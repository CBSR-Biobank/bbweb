/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'lodash'], function(angular, _) {
  'use strict';

  StudyAnnotationTypeFactory.$inject = [
    'funutils',
    'biobankApi',
    'AnnotationValueType',
    'DomainError',
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
                                      DomainError,
                                      validationService,
                                      studyAnnotationTypeValidation,
                                      AnnotationType) {

    function StudyAnnotationType(obj) {
      var defaults = { studyId: null };

      obj = obj || {};
      AnnotationType.call(this, obj);
      _.extend(this, defaults, _.pick(obj, _.keys(defaults)));
      this._service = null;
    }

    StudyAnnotationType.prototype = Object.create(AnnotationType.prototype);

    StudyAnnotationType.prototype.constructor = StudyAnnotationType;

    StudyAnnotationType.create = function(obj) {
      var annotationType = AnnotationType.create(obj);
      if (!_.isObject(annotationType)) {
        return annotationType;
      }
      if (!obj.studyId) {
        return new Error('invalid object from server: missing studyId');
      }
      return new StudyAnnotationType(obj);
    };

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
        '/studies/' + studyId + '/' + annotationTypeUriPart + '?annotTypeId=' + annotationTypeId
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
        throw new DomainError('_service is null');
      }

      return self._service.addOrUpdate(self).then(function(reply) {
        return create(validator, createFn, reply);
      });
    };

    StudyAnnotationType.prototype.remove = function () {
      var self = this;
      if (self._service === null) {
        throw new DomainError('_service is null');
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
