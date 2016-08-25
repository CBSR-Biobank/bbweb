/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'lodash'], function(angular, _) {
  'use strict';

  studyAnnotationTypesServiceFactory.$inject = [
    'biobankApi',
    'domainNotificationService'
  ];

  function studyAnnotationTypesServiceFactory(biobankApi,
                                         domainNotificationService,
                                         ParticipantAnnotationType) {

    /**
     * Service to access study annotation types.
     */
    function StudyAnnotationTypesService(annotationTypeUri) {
      this.annotationTypeUri = annotationTypeUri || '';
    }

    function uri(/* annotationTypeUri, studyId, annotationTypeId, version */) {
      var args = _.toArray(arguments);
      var annotationTypeUri, studyId, annotationTypeId, version;
      var result = '/studies';

      if (arguments.length < 2) {
        throw new Error('annotationTypeUri or study id not specified');
      }

      annotationTypeUri = args.shift();
      studyId = args.shift();

      result += '/' + studyId + '/' + annotationTypeUri;

      if (args.length > 0) {
        annotationTypeId = args.shift();
        result += '/' + annotationTypeId;
      }

      if (args.length > 0) {
        version = args.shift();
        result += '/' + version;
      }
      return result;
    }

    StudyAnnotationTypesService.prototype.addOrUpdate = function (annotationType) {
      if (annotationType.isNew()) {
        return biobankApi.post(
          uri(this.annotationTypeUri, annotationType.studyId),
          getAddCommand(annotationType));
      }

      return biobankApi.put(
        uri(this.annotationTypeUri, annotationType.studyId, annotationType.id),
        getUpdateCommand(annotationType));

    };

    StudyAnnotationTypesService.prototype.remove = function (annotationType) {
      return biobankApi.del(
        uri(this.annotationTypeUri, annotationType.studyId, annotationType.id, annotationType.version));
    };

    function getAddCommand(annotationType) {
      var cmd = _.pick(annotationType, ['studyId', 'name', 'valueType', 'options']);
      if (annotationType.description) {
        cmd.description = annotationType.description;
      }
      if (annotationType.isValueTypeSelect()) {
        cmd.maxValueCount = annotationType.maxValueCount;
      }
      if (!_.isUndefined(annotationType.required)) {
        cmd.required = annotationType.required;
      }
      return cmd;
    }

    function getUpdateCommand (annotationType) {
      return _.extend(getAddCommand(annotationType), {
        id:              annotationType.id,
        expectedVersion: annotationType.version
      });
    }

    return StudyAnnotationTypesService;
  }

  return studyAnnotationTypesServiceFactory;
});
