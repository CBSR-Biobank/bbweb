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

  describe('CollectionEventAnnotationType', function() {

    var context = {}, CollectionEventAnnotationType, jsonEntities;
    var requiredKeys = ['id', 'studyId', 'name', 'valueType', 'options'];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_CollectionEventAnnotationType_,
                               jsonEntities) {
      CollectionEventAnnotationType = _CollectionEventAnnotationType_;
      jsonEntities = jsonEntities;

      context.annotationTypeType            = CollectionEventAnnotationType;
      context.createAnnotationTypeFn        = createAnnotationType;
      context.annotationTypeUriPart         = '/ceannottypes';
      context.objRequiredKeys               = requiredKeys;
      context.createServerAnnotationTypeFn  = createServerAnnotationType;
      context.annotationTypeListFn          = CollectionEventAnnotationType.list;
      context.annotationTypeGetFn           = CollectionEventAnnotationType.get;
    }));

    function createServerAnnotationType(options) {
      var study = jsonEntities.study();
      options = options || {};
      return jsonEntities.studyAnnotationType(study, options);
    }

    function createAnnotationType(obj) {
      obj = obj || {};
      return new CollectionEventAnnotationType(obj);
    }

    studyAnnotationTypeSharedSpec(context);

  });


});
