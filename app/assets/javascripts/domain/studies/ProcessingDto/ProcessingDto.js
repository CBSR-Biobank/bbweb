/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/* @ngInject */
function ProcessingDtoFactory(funutils,
                              validationService,
                              biobankApi,
                              ProcessingType,
                              SpecimenLinkType,
                              SpecimenLinkAnnotationType,
                              SpecimenGroup) {

  var requiredKeys = [
    'processingTypes',
    'specimenLinkTypes',
    'specimenLinkAnnotationTypes',
    'specimenLinkAnnotationTypeIdsInUse',
    'specimenGroups'
  ];

  var validateObj = funutils.partial(
    validationService.condition1(
      validationService.validator('must be a map', _.isObject),
      validationService.validator('has the correct keys',
                                  validationService.hasKeys.apply(null, requiredKeys))),
    _.identity);

  /**
   * An object that contains a list of all the processingTypes, specimenLinkTypes,
   * specimenLinkAnnotationTypes, and specimenGroups for a study.
   */
  function ProcessingDto(obj) {
    var self = this,
        defaults = {
          processingTypes:                    [],
          specimenLinkTypes:                  [],
          specimenLinkAnnotationTypes:        [],
          specimenGroups:                     [],
          specimenLinkAnnotationTypeIdsInUse: []
        };

    obj = obj || {};
    Object.assign(this, defaults, _.pick(obj, 'specimenLinkAnnotationTypeIdsInUse'));

    self.processingTypes = obj.processingTypes.map((serverPt) => new ProcessingType(serverPt));

    self.specimenLinkTypes = obj.specimenLinkTypes
      .map((serverSlt) => new SpecimenLinkType(serverSlt, {
        studySpecimenGroups:  obj.specimenGroups,
        studyAnnotationTypes: obj.specimenLinkAnnotationTypes
      }));

    self.specimenLinkAnnotationTypes = obj.specimenLinkAnnotationTypes
      .map((serverAt) => new SpecimenLinkAnnotationType(serverAt));

    self.specimenGroups = obj.specimenGroups.map((serverSg) => new SpecimenGroup(serverSg));
  }

  /**
   * Used by promise code, so it must return an error rather than throw one.
   */
  ProcessingDto.create = function (obj) {
    var validation = validateObj(obj);
    if (!_.isObject(validation)) {
      return new Error('invalid object from server: ' + validation);
    }
    return new ProcessingDto(obj);
  };

  ProcessingDto.get = function(studyId) {
    return biobankApi.get('/studies/' + studyId + '/dto/processing')
      .then(function(reply) {
        return ProcessingDto.create(reply);
      });
  };

  return ProcessingDto;
}

export default ngModule => ngModule.factory('ProcessingDto', ProcessingDtoFactory)
