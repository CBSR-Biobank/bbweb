/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 *
 */
/* @ngInject */
function SpecimenLinkAnnotationTypeFactory(StudyAnnotationType,
                                           studyAnnotationTypeValidation,
                                           spcLinkAnnotationTypesService) {

  function SpecimenLinkAnnotationType(obj) {
    obj = obj || {};
    StudyAnnotationType.call(this, obj);

    this._service = spcLinkAnnotationTypesService;
  }

  SpecimenLinkAnnotationType.prototype = Object.create(StudyAnnotationType.prototype);

  SpecimenLinkAnnotationType.prototype.constructor = SpecimenLinkAnnotationType;

  SpecimenLinkAnnotationType.create = function(obj) {
    var annotationType = StudyAnnotationType.create(obj);
    if (!_.isObject(annotationType)) {
      return annotationType;
    }
    return new SpecimenLinkAnnotationType(obj);
  };

  SpecimenLinkAnnotationType.list = function(studyId) {
    return StudyAnnotationType.list(studyAnnotationTypeValidation.validateObj,
                                    SpecimenLinkAnnotationType.create,
                                    'slannottypes',
                                    studyId);
  };

  SpecimenLinkAnnotationType.get = function(studyId, annotationTypeId) {
    return StudyAnnotationType.get(studyAnnotationTypeValidation.validateObj,
                                   SpecimenLinkAnnotationType.create,
                                   'slannottypes',
                                   studyId,
                                   annotationTypeId);
  };

  SpecimenLinkAnnotationType.prototype.addOrUpdate = function () {
    return StudyAnnotationType.prototype
      .addOrUpdate.call(this,
                        studyAnnotationTypeValidation.validateObj,
                        SpecimenLinkAnnotationType.create);
  };

  return SpecimenLinkAnnotationType;
}

export default ngModule => ngModule.factory('SpecimenLinkAnnotationType', SpecimenLinkAnnotationTypeFactory)
