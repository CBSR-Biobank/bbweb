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

      context.annotTypeType            = SpecimenLinkAnnotationType;
      context.createAnnotTypeFn        = createAnnotType;
      context.annotTypeUriPart         = '/slannottypes';
      context.objRequiredKeys          = requiredKeys;
      context.createServerAnnotTypeFn  = createServerAnnotType;
      context.annotTypeListFn          = SpecimenLinkAnnotationType.list;
      context.annotTypeGetFn           = SpecimenLinkAnnotationType.get;
    }));

    function createServerAnnotType(options) {
      var study = fakeEntities.study();
      options = options || {};
      return fakeEntities.studyAnnotationType(study, options);
    }

    function createAnnotType(obj) {
      obj = obj || {};
      return new SpecimenLinkAnnotationType(obj);
    }

    studyAnnotationTypeSharedSpec(context);

  });


});
