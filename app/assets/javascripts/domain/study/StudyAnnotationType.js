define(['../module', 'angular', 'underscore'], function(module, angular, _) {
  'use strict';

  module.factory('StudyAnnotationType', StudyAnnotationTypeFactory);

  StudyAnnotationTypeFactory.$inject = [
    'funutils',
    'biobankApi',
    'validationService',
    'studyAnnotationTypeValidation',
    'AnnotationType'
  ];

  /**
   *
   */
  function StudyAnnotationTypeFactory(funutils,
                                      biobankApi,
                                      validationService,
                                      studyAnnotationTypeValidation,
                                      AnnotationType) {

    function StudyAnnotationType(obj) {
      obj = obj || {};
      AnnotationType.call(this, obj);
      _.extend(this, _.defaults(obj, { studyId: null }));

      this._service = null;
      this._validateAddedEvent = studyAnnotationTypeValidation.validateAddedEvent;
      this._validateUpdatedEvent = studyAnnotationTypeValidation.validateUpdatedEvent;
    }

    StudyAnnotationType.prototype = Object.create(AnnotationType.prototype);

    /**
     * Factory function to get all study annotation types for studyId.
     *
     * @param {String} studyId - the ID for the parent study.
     */
    StudyAnnotationType.list = function (validator, createFn, annotTypeUriPart, studyId) {
      return biobankApi.call('GET', '/studies/' + studyId + '/' + annotTypeUriPart).then(function (reply) {
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
      return biobankApi.call(
        'GET',
        '/studies/' + studyId + '/' + annotTypeUriPart + '/' + annotationTypeId
      ).then(function (obj) {
        return create(validator, createFn, obj);
      });
    };

    /**
     * Sends a command, to the server, to add or update to a study annotation type.
     */
    StudyAnnotationType.prototype.addOrUpdate = function () {
      var self = this;
      if (self._service === null) {
        throw new Error('_service is null');
      }

      if (self._validateAddedEvent === null) {
        return new Error('self._validateAddedEvent is null');
      }

      if (self._validateUpdatedEvent === null) {
        return new Error('self._validateUpdatedEvent is null');
      }

      return self._service.addOrUpdate(self).then(function(event) {
        var validator = self.isNew() ? self._validateAddedEvent : self._validateUpdatedEvent;

        var result = validator(event);

        if (!_.isObject(result)) {
          return new Error('invalid event from server: ' + result);
        }

        return _.extend(funutils.renameKeys(result, { annotationTypeId: 'id' }), { version: 0 });
      });
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

});
