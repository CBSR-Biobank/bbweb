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

  describe('SpecimenLinkAnnotationType', function() {

    var context = {}, SpecimenLinkAnnotationType, fakeEntities;
    var requiredKeys = ['id', 'studyId', 'name', 'valueType', 'options'];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_SpecimenLinkAnnotationType_,
                               fakeDomainEntities) {
      SpecimenLinkAnnotationType = _SpecimenLinkAnnotationType_;
      fakeEntities = fakeDomainEntities;

      context.annotationTypeType            = SpecimenLinkAnnotationType;
      context.createAnnotationTypeFn        = createAnnotationType;
      context.annotationTypeUriPart         = '/slannottypes';
      context.objRequiredKeys          = requiredKeys;
      context.createServerAnnotationTypeFn  = createServerAnnotationType;
      context.annotationTypeListFn          = SpecimenLinkAnnotationType.list;
      context.annotationTypeGetFn           = SpecimenLinkAnnotationType.get;
    }));

    function createServerAnnotationType(options) {
      var study = fakeEntities.study();
      options = options || {};
      return fakeEntities.studyAnnotationType(study, options);
    }

    function createAnnotationType(obj) {
      obj = obj || {};
      return new SpecimenLinkAnnotationType(obj);
    }

    studyAnnotationTypeSharedSpec(context);

  });


});
