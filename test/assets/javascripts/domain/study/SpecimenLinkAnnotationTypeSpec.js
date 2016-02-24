/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  './studyAnnotationTypeSharedSpec',
  'biobankApp'
], function(angular, mocks, studyAnnotationTypeSharedSpec) {
  'use strict';

  describe('SpecimenLinkAnnotationType', function() {

    var context = {}, SpecimenLinkAnnotationType, jsonEntities;
    var requiredKeys = ['id', 'studyId', 'name', 'valueType', 'options'];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_SpecimenLinkAnnotationType_,
                               jsonEntities) {
      SpecimenLinkAnnotationType = _SpecimenLinkAnnotationType_;
      jsonEntities = jsonEntities;

      context.annotationTypeType            = SpecimenLinkAnnotationType;
      context.createAnnotationTypeFn        = createAnnotationType;
      context.annotationTypeUriPart         = '/slannottypes';
      context.objRequiredKeys          = requiredKeys;
      context.createServerAnnotationTypeFn  = createServerAnnotationType;
      context.annotationTypeListFn          = SpecimenLinkAnnotationType.list;
      context.annotationTypeGetFn           = SpecimenLinkAnnotationType.get;
    }));

    function createServerAnnotationType(options) {
      var study = jsonEntities.study();
      options = options || {};
      return jsonEntities.studyAnnotationType(study, options);
    }

    function createAnnotationType(obj) {
      obj = obj || {};
      return new SpecimenLinkAnnotationType(obj);
    }

    studyAnnotationTypeSharedSpec(context);

  });


});
