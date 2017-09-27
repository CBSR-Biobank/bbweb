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

  xdescribe('SpecimenLinkAnnotationType', function() {

    var context = {}, SpecimenLinkAnnotationType, factory;
    var requiredKeys = ['id', 'studyId', 'name', 'valueType', 'options'];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_SpecimenLinkAnnotationType_,
                               factory) {
      SpecimenLinkAnnotationType = _SpecimenLinkAnnotationType_;
      factory = factory;

      context.annotationTypeType            = SpecimenLinkAnnotationType;
      context.createAnnotationTypeFn        = createAnnotationType;
      context.annotationTypeUriPart         = '/slannottypes';
      context.objRequiredKeys          = requiredKeys;
      context.createServerAnnotationTypeFn  = createServerAnnotationType;
      context.annotationTypeListFn          = SpecimenLinkAnnotationType.list;
      context.annotationTypeGetFn           = SpecimenLinkAnnotationType.get;
    }));

    function createServerAnnotationType(options) {
      var study = factory.study();
      options = options || {};
      return factory.studyAnnotationType(study, options);
    }

    function createAnnotationType(obj) {
      obj = obj || {};
      return new SpecimenLinkAnnotationType(obj);
    }

    studyAnnotationTypeSharedSpec(context);

  });


});
