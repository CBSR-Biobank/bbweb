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

  describe('ParticipantAnnotationType', function() {

    var context = {}, ParticipantAnnotationType, fakeEntities;
    var commonKeys = ['required', 'studyId', 'name', 'valueType', 'options'];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_ParticipantAnnotationType_,
                               fakeDomainEntities) {

      ParticipantAnnotationType = _ParticipantAnnotationType_;
      fakeEntities = fakeDomainEntities;

      context.annotTypeType            = ParticipantAnnotationType;
      context.createAnnotTypeFn        = createAnnotType;
      context.annotTypeUriPart         = '/pannottypes';
      context.objRequiredKeys          = commonKeys.concat('id');
      context.addedEventRequiredKeys   = commonKeys.concat('annotationTypeId');
      context.updatedEventRequiredKeys = context.addedEventRequiredKeys.concat('version');
      context.createServerAnnotTypeFn  = createServerAnnotType;
      context.annotTypeListFn          = ParticipantAnnotationType.list;
      context.annotTypeGetFn           = ParticipantAnnotationType.get;
    }));

    function createServerAnnotType() {
      var study = fakeEntities.study();
      return fakeEntities.studyAnnotationType(study, { required: true });
    }

    function createAnnotType(obj) {
      obj = obj || {};
      return new ParticipantAnnotationType(obj);
    }

    studyAnnotationTypeSharedSpec(context);

  });


});
