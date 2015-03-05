define(['../module', 'angular', 'underscore'], function(module, angular, _) {
  'use strict';

  module.factory('StudyAnnotationType', StudyAnnotationTypeFactory);

  StudyAnnotationTypeFactory.$inject = [
    'validationService',
    'AnnotationType'
  ];

  /**
   *
   */
  function StudyAnnotationTypeFactory(validationService,
                                      AnnotationType) {

    function StudyAnnotationType(obj) {
      obj = obj || {};
      AnnotationType.call(this, obj);
      _.extend(this, _.defaults(obj, { studyId: null }));

      this._requiredKeys.unshift('studyId');
      this._addedEventRequiredKeys = ['studyId', 'annotationTypeId', 'name', 'valueType', 'options'];
      this._updatedEventRequiredKeys = this._addedEventRequiredKeys.concat('version');

      this._service = null;
    }

    StudyAnnotationType.prototype = Object.create(AnnotationType.prototype);

    /**
     * Used to create a study annotation type from a response from the server.
     *
     * The object is validated to have the required fields.
     */
    StudyAnnotationType.create = function (obj) {
      var checker = validationService.checker(
        validationService.aMapValidator,
        validationService.hasKeys.apply(null, this._requiredKeys));
      var checks = checker(obj);

      if (checks.length > 0) {
        return new Error('invalid object from server: ' + checks.join(', '));
      }
      return new StudyAnnotationType(obj);
    };

    StudyAnnotationType.prototype.addOrUpdate = function () {
      var self = this;
      if (self._service === null) {
        throw new Error('_service is null');
      }
      return self._service.addOrUpdate(self).then(function(event) {
        var checks = addOrUpdateChecker()(event);

        if (checks.length > 0) {
          //console.log(event);
          return new Error('invalid event from server: ' + checks.join(', '));
        }

        // FIXME use renameKeys here
        _.extend(self, _.defaults(
          _.omit(event, 'participantId'),
          { id: event.participantId, version: 0 }));
        return self;
      });

      function addOrUpdateChecker() {
        var requiredKeys = self.isNew() ? self._addedEventRequiredKeys : self._updatedEventRequiredKeys;

        return validationService.checker(
          validationService.aMapValidator,
          validationService.hasKeys.apply(null, requiredKeys));
      }
    };

    return StudyAnnotationType;
  }

});
