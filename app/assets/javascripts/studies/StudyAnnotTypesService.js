define(['./module', 'angular'], function(module, angular) {
  'use strict';

  module.factory('StudyAnnotTypesService', StudyAnnotTypesServiceFactory);

  StudyAnnotTypesServiceFactory.$inject = ['biobankApi', 'domainEntityService'];

  function StudyAnnotTypesServiceFactory(biobankApi, domainEntityService) {

    /**
     * Service to access study annotation types.
     */
    function StudyAnnotTypesService(annotTypeUri) {
      this.annotTypeUri = annotTypeUri || '';
    }

    function uri(annotTypeUri, studyId, annotTypeId, version) {
      var result = '/studies';
      if (arguments.length < 2) {
        throw new Error('annotTypeUri or study id not specified');
      } else {
        result += '/' + studyId + '/' + annotTypeUri;

        if (arguments.length > 2) {
          result += '/' + annotTypeId;
        }

        if (arguments.length > 3) {
          result += '/' + version;
        }
      }
      return result;
    }

    StudyAnnotTypesService.prototype.getAll = function (annotationTypeType, studyId) {
      return biobankApi.call('GET', uri(this.annotTypeUri, studyId)).then(function (annotationTypes) {
          return _.map(annotationTypes, function (at) {
            return new annotationTypeType(at);
          });
      });
    };

    StudyAnnotTypesService.prototype.get = function (annotationTypeType, studyId, annotTypeId) {
      return biobankApi.call('GET', uri(this.annotTypeUri, studyId) + '?annotTypeId=' + annotTypeId)
        .then(function (annotationType) {
          return new annotationTypeType(annotationType);
        });
    };

    StudyAnnotTypesService.prototype.addOrUpdate = function (annotType) {
      var cmd;

      if (annotType.isNew()) {
        cmd = annotType.getAddCommand(annotType);
        return biobankApi.call('POST', uri(this.annotTypeUri, annotType.studyId), cmd);
      } else {
        cmd = annotType.getUpdateCommand(annotType);
        return biobankApi.call('PUT', uri(this.annotTypeUri, annotType.studyId, annotType.id), cmd);
      }
    };

    StudyAnnotTypesService.prototype.remove = function (annotType) {
      return biobankApi.call(
        'DELETE',
        uri(this.annotTypeUri, annotType.studyId, annotType.id, annotType.version));
    };

    return StudyAnnotTypesService;

  }

});
