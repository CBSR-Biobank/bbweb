define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  studyAnnotTypesServiceFactory.$inject = [
    'biobankApi',
    'domainEntityService'
  ];

  function studyAnnotTypesServiceFactory(biobankApi,
                                         domainEntityService,
                                         ParticipantAnnotationType) {

    /**
     * Service to access study annotation types.
     */
    function StudyAnnotTypesService(annotTypeUri) {
      this.annotTypeUri = annotTypeUri || '';
    }

    function uri(/* annotTypeUri, studyId, annotTypeId, version */) {
      var args = _.toArray(arguments);
      var annotTypeUri, studyId, annotTypeId, version;
      var result = '/studies';

      if (arguments.length < 2) {
        throw new Error('annotTypeUri or study id not specified');
      }

      annotTypeUri = args.shift();
      studyId = args.shift();

      result += '/' + studyId + '/' + annotTypeUri;

      if (args.length > 0) {
        annotTypeId = args.shift();
        result += '/' + annotTypeId;
      }

      if (args.length > 0) {
        version = args.shift();
        result += '/' + version;
      }
      return result;
    }

    StudyAnnotTypesService.prototype.addOrUpdate = function (annotType) {
      if (annotType.isNew()) {
        return biobankApi.post(
          uri(this.annotTypeUri, annotType.studyId),
          getAddCommand(annotType));
      }

      return biobankApi.put(
        uri(this.annotTypeUri, annotType.studyId, annotType.id),
        getUpdateCommand(annotType));

    };

    StudyAnnotTypesService.prototype.remove = function (annotType) {
      return biobankApi.del(
        uri(this.annotTypeUri, annotType.studyId, annotType.id, annotType.version));
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

    return StudyAnnotTypesService;
  }

  return studyAnnotTypesServiceFactory;
});
