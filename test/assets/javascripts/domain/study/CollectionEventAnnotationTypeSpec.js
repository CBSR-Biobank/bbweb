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

      context.annotTypeType            = CollectionEventAnnotationType;
      context.createAnnotTypeFn        = createAnnotType;
      context.annotTypeUriPart         = '/ceannottypes';
      context.objRequiredKeys          = requiredKeys;
      context.createServerAnnotTypeFn  = createServerAnnotType;
      context.annotTypeListFn          = CollectionEventAnnotationType.list;
      context.annotTypeGetFn           = CollectionEventAnnotationType.get;
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
