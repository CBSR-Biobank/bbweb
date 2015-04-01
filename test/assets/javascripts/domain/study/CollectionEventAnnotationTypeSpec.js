/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  './studyAnnotationTypeSharedSpec',
  'biobankApp'
], function(angular, mocks, studyAnnotationTypeSharedSpec) {
  'use strict';

  describe('CollectionEventAnnotationType', function() {

    var context = {}, CollectionEventAnnotationType, fakeEntities;
    var requiredKeys = ['id', 'studyId', 'name', 'valueType', 'options'];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_CollectionEventAnnotationType_,
                               fakeDomainEntities) {
      CollectionEventAnnotationType = _CollectionEventAnnotationType_;
      fakeEntities = fakeDomainEntities;

      context.annotationTypeType            = CollectionEventAnnotationType;
      context.createAnnotTypeFn        = createAnnotType;
      context.annotationTypeUriPart         = '/ceannottypes';
      context.objRequiredKeys          = requiredKeys;
      context.createServerAnnotTypeFn  = createServerAnnotType;
      context.annotationTypeListFn          = CollectionEventAnnotationType.list;
      context.annotationTypeGetFn           = CollectionEventAnnotationType.get;
    }));

    function createServerAnnotType(options) {
      var study = fakeEntities.study();
      options = options || {};
      return fakeEntities.studyAnnotationType(study, options);
    }

    function createAnnotType(obj) {
      obj = obj || {};
      return new CollectionEventAnnotationType(obj);
    }

    studyAnnotationTypeSharedSpec(context);

  });


});
