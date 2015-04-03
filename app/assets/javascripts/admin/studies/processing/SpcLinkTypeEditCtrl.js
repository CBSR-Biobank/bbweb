/**
 * Study administration controllers.
 */
define(['underscore'], function(_) {
  'use strict';

  SpcLinkTypeEditCtrl.$inject = [
    '$state',
    'SpecimenLinkType',
    'SpecimenGroup',
    'domainEntityService',
    'notificationsService',
    'study',
    'spcLinkType',
    'processingDto'
  ];

  /**
   * Add Specimen Link Type
   */
  function SpcLinkTypeEditCtrl($state,
                               SpecimenLinkType,
                               SpecimenGroup,
                               domainEntityService,
                               notificationsService,
                               study,
                               spcLinkType,
                               processingDto) {
    var vm = this, action;

    vm.study                 = study;
    vm.processingTypes       = processingDto.processingTypes;
    vm.processingTypesById   = _.indexBy(processingDto.processingTypes, 'id');
    vm.studyAnnotationTypes  = processingDto.specimenLinkAnnotationTypes;
    vm.studySpecimenGroups   = processingDto.specimenGroups;

    vm.submit                = submit;
    vm.cancel                = cancel;
    vm.addAnnotationType     = addAnnotationType;
    vm.removeAnnotationType  = removeAnnotationType;
    vm.getSpecimenGroupUnits = getSpecimenGroupUnits;

    vm.specimenLinkType = new SpecimenLinkType(
      spcLinkType, {
        studySpecimenGroups:  processingDto.specimenGroups,
        studyAnnotationTypes: processingDto.specimenLinkAnnotationTypes
      });

    action = vm.specimenLinkType.isNew() ? 'Add' : 'Update';
    vm.title =  action + ' Specimen Link Type';

    //---

    function gotoReturnState() {
      return $state.go('home.admin.studies.study.processing', {}, {reload: true});
    }

    function submitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState();
    }

    function submit(specimenLinkType) {
      specimenLinkType.addOrUpdate()
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityService.updateErrorModal(
            error, 'collection event type');
        });
    }

    function cancel() {
      gotoReturnState();
    }

    function addAnnotationType () {
      vm.specimenLinkType.annotationTypeData.push({annotationTypeId:'', required: false});
    }

    function removeAnnotationType (index) {
      if ((index < 0) || (index >= vm.specimenLinkType.annotationTypeData.length)) {
        throw new Error('index is invalid: ' + index);
      }
      vm.specimenLinkType.annotationTypeData.splice(index, 1);
    }

    function getSpecimenGroupUnits(sgId) {
      return SpecimenGroup.getUnits(vm.studySpecimenGroups, sgId);
    }
  }

  return SpcLinkTypeEditCtrl;
});
