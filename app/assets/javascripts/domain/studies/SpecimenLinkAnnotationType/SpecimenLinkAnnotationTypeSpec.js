/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import angular from 'angular';
import sharedBehaviour from 'test/behaviours/studyAnnotationTypeSharedBehaviour';

const ngModule = 'FIXME'

xdescribe('SpecimenLinkAnnotationType', function() {

  var context = {}, SpecimenLinkAnnotationType, Factory;
  var requiredKeys = ['id', 'studyId', 'name', 'valueType', 'options'];

  beforeEach(angular.mock.module(ngModule, 'biobank.test'));

  beforeEach(angular.mock.inject(function(_SpecimenLinkAnnotationType_, _Factory_) {
    SpecimenLinkAnnotationType = _SpecimenLinkAnnotationType_;
    Factory = _Factory_;

    context.annotationTypeType            = SpecimenLinkAnnotationType;
    context.createAnnotationTypeFn        = createAnnotationType;
    context.annotationTypeUriPart         = '/slannottypes';
    context.objRequiredKeys          = requiredKeys;
    context.createServerAnnotationTypeFn  = createServerAnnotationType;
    context.annotationTypeListFn          = SpecimenLinkAnnotationType.list;
    context.annotationTypeGetFn           = SpecimenLinkAnnotationType.get;
  }));

  function createServerAnnotationType(options) {
    var study = Factory.study();
    options = options || {};
    return Factory.studyAnnotationType(study, options);
  }

  function createAnnotationType(obj) {
    obj = obj || {};
    return new SpecimenLinkAnnotationType(obj);
  }

  sharedBehaviour(context);

});
